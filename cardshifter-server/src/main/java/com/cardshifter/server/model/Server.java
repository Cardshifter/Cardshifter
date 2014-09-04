package com.cardshifter.server.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.incoming.LoginMessage;
import com.cardshifter.server.incoming.StartGameRequest;
import com.cardshifter.server.incoming.UseAbilityMessage;
import com.cardshifter.server.messages.Message;

public class Server {
	private static final Logger	logger = LogManager.getLogger(Server.class);

	private static final String VANILLA = "vanilla";

	// Counters for various things
	private final AtomicInteger roomCounter = new AtomicInteger(0);
	private final AtomicInteger inviteId = new AtomicInteger(0);
	private final AtomicInteger gameId = new AtomicInteger(0);
	
	private final IncomingHandler incomingHandler;
	
	private final Set<ClientIO> clients = Collections.synchronizedSet(new HashSet<>());
	private final Map<Integer, ChatArea> chats = new ConcurrentHashMap<>();
	private final Map<Integer, ServerGame> games = new ConcurrentHashMap<>();
	private final Map<Integer, GameInvite> invites = new ConcurrentHashMap<>();
	private final Map<String, GameFactory> gameFactories = new ConcurrentHashMap<>();

	private final Set<ConnectionHandler> handlers = Collections.synchronizedSet(new HashSet<>());
	private final AtomicReference<ClientIO> playAny = new AtomicReference<>();
	private final Random random = new Random();

	public Server() {
		this.incomingHandler = new IncomingHandler(this);
		this.newChatRoom("Main");
		
		Server server = this;
		IncomingHandler incomings = server.getIncomingHandler();
		
		Handlers handlers = new Handlers(this);
		
		incomings.addHandler("login", LoginMessage.class, handlers::loginMessage);
//		incomings.addHandler("chat", ChatMessage.class);
		incomings.addHandler("startgame", StartGameRequest.class, handlers::play);
		incomings.addHandler("use", UseAbilityMessage.class, handlers::useAbility);
		
		server.addGameFactory(VANILLA, (serv, id) -> new TCGGame(serv, id));
		
	}
	
	@Deprecated
	private ChatArea getMainChat() {
		return chats.get(0);
	}
	
	public ChatArea newChatRoom(String name) {
		int id = roomCounter.getAndIncrement();
		ChatArea room = new ChatArea(roomCounter.getAndIncrement(), name);
		chats.put(id, room);
		return room;
	}
	
	public Collection<ClientIO> getClients() {
		return new ArrayList<>(clients);
	}
	
	public IncomingHandler getIncomingHandler() {
		return incomingHandler;
	}

	public void handleMessage(ClientIO client, String json) {
		Objects.requireNonNull(client, "Cannot handle message from a null client");
		logger.info("Handle message " + client + ": " + json);
		Message message;
		try {
			message = incomingHandler.parse(json);
			logger.info("Parsed Message: " + message);
			incomingHandler.perform(message, client);
		} catch (Exception e) {
			logger.error("Unable to parse incoming json: " + json, e);
		}
	}

	public void newClient(ClientIO cl) {
		logger.info("New client: " + cl);
		clients.add(cl);
		getMainChat().add(cl);
	}
	
	public void onDisconnected(ClientIO client) {
		clients.remove(client);
		getMainChat().remove(client);
	}

	void broadcast(String data) {
		clients.forEach(cl -> cl.sendToClient(data));
	}

	public void incomingChatMessage(Command cmd) {
		ChatArea room = this.chats.get(cmd.getParameterInt(1));
		if (room == null) {
			cmd.getSender().sendToClient("INVALID CHAT ROOM");
			return;
		}
		room.broadcast(cmd.getFullCommand(2));
	}
	
	public void addGameFactory(String gameType, GameFactory factory) {
		this.gameFactories.put(gameType, factory);
	}

	public boolean inviteRequest(Command cmd) {
		final GameInvite invite;
		switch (cmd.getCommand()) {
			case "INVT":
				String target = cmd.getParameter(2);
				ServerGame game = createGame(cmd.getParameter(1));
				if (game == null) {
					cmd.getSender().sendToClient("FAIL Game creation failed");
					return false;
				}
				invite = new GameInvite(this, inviteId.getAndIncrement(), cmd, game);
				this.invites.put(invite.getId(), invite);
				
				Stream<ClientIO> targetStream = clients.stream().filter(cl -> cl.getName().equals(target));
				Optional<ClientIO> result = targetStream.findFirst();
				if (result.isPresent()) {
					invite.sendInvite(result.get());
				}
				else cmd.getSender().sendToClient("FAIL No such user");
				return result.isPresent();
			case "INVY":
				invite = invites.get(cmd.getParameterInt(1));
				if (invite == null) {
					cmd.getSender().sendToClient("FAIL Invalid invite id");
					return false;
				}
				return invite.inviteAccept(cmd.getSender());
			case "INVN":
				invite = invites.get(cmd.getParameterInt(1));
				if (invite == null) {
					cmd.getSender().sendToClient("FAIL Invalid invite id");
					return false;
				}
				return invite.inviteDecline(cmd.getSender());
			default:
				throw new AssertionError("Invalid command: " + cmd);
		}
	}
	
	public void incomingGameCommand(Command cmd) {
		ServerGame game = games.get(cmd.getParameterInt(1));
		if (game != null) {
			if (!game.handleMove(cmd))
				cmd.getSender().sendToClient("FAIL Invalid move");
		}
		else cmd.getSender().sendToClient("FAIL Invalid gameid");
	}

	private ServerGame createGame(String parameter) {
		GameFactory suppl = gameFactories.get(parameter);
		if (suppl == null) {
			throw new IllegalArgumentException("No such game factory: " + parameter);
		}
		ServerGame game = suppl.newGame(this, gameId.getAndIncrement());
		this.games.put(game.getId(), game);
		return suppl != null ? game : null;
	}
	
	public Map<Integer, ChatArea> getChats() {
		return new HashMap<>(chats);
	}
	
	public Map<Integer, ServerGame> getGames() {
		return new HashMap<>(games);
	}
	
	public Map<Integer, GameInvite> getInvites() {
		return new HashMap<>(invites);
	}

	public void addConnections(ConnectionHandler handler) {
		handler.start();
		this.handlers.add(handler);
	}

	public AtomicReference<ClientIO> getPlayAny() {
		return playAny;
	}

	public void newGame(ClientIO client, ClientIO opponent) {
		ServerGame game = this.gameFactories.get(VANILLA).newGame(this, this.gameId.getAndIncrement());
		List<ClientIO> players = Arrays.asList(client, opponent);
		Collections.shuffle(players, random);
		
		this.games.put(game.getId(), game);
		game.start(players);
	}
	
}
