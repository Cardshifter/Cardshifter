package com.cardshifter.server.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.incoming.ChatMessage;
import com.cardshifter.server.incoming.Message;
import com.cardshifter.server.incoming.PlayCardMessage;
import com.cardshifter.server.incoming.UseAbilityMessage;


public class Server {
	private static final Logger	logger = LogManager.getLogger(Server.class);
	
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

	public Server() {
		this.incomingHandler = new IncomingHandler(this);
		this.newChatRoom("Main");
		
		Server server = this;
		IncomingHandler incomings = server.getIncomingHandler();
		
		incomings.addHandler("chat", ChatMessage.class);
		incomings.addHandler("playCard", PlayCardMessage.class);
		incomings.addHandler("useAbility", UseAbilityMessage.class);
		
//		server.addGameFactory("default", (serv, id) -> new Game(serv, id));
		
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
		Message message;
		try {
			message = incomingHandler.parse(json);
			message.perform(client);
		} catch (IOException e) {
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

}
