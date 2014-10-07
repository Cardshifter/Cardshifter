package com.cardshifter.server.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.both.InviteResponse;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.RequestTargetsMessage;
import com.cardshifter.api.incoming.ServerQueryMessage;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.ClientDisconnectedMessage;
import com.cardshifter.api.outgoing.ServerErrorMessage;
import com.cardshifter.api.outgoing.UserStatusMessage;
import com.cardshifter.api.outgoing.UserStatusMessage.Status;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Server {
	private static final Logger	logger = LogManager.getLogger(Server.class);

	// Counters for various things
	private final AtomicInteger clientId = new AtomicInteger(0);
	private final AtomicInteger roomCounter = new AtomicInteger(0);
	private final AtomicInteger gameId = new AtomicInteger(0);
	
	private final IncomingHandler incomingHandler;
	
	private final Map<Integer, ClientIO> clients = new ConcurrentHashMap<>();
	private final Map<Integer, ChatArea> chats = new ConcurrentHashMap<>();
	private final Map<Integer, ServerGame> games = new ConcurrentHashMap<>();
	private final ServerHandler<GameInvite> invites = new ServerHandler<>();
	private final Map<String, GameFactory> gameFactories = new ConcurrentHashMap<>();

	private final Set<ConnectionHandler> handlers = Collections.synchronizedSet(new HashSet<>());
	private final AtomicReference<ClientIO> playAny = new AtomicReference<>();

	private final ScheduledExecutorService scheduler;
	private final ChatArea mainChat;

	public Server() {
		this.incomingHandler = new IncomingHandler(this);
		this.scheduler = Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder().setNameFormat("ai-thread-%d").build());
		mainChat = this.newChatRoom("Main");
		
		Handlers handlers = new Handlers(this);
		
		incomingHandler.addHandler("login", LoginMessage.class, handlers::loginMessage);
		incomingHandler.addHandler("chat", ChatMessage.class, handlers::chat);
		incomingHandler.addHandler("startgame", StartGameRequest.class, handlers::play);
		incomingHandler.addHandler("use", UseAbilityMessage.class, handlers::useAbility);
		incomingHandler.addHandler("requestTargets", RequestTargetsMessage.class, handlers::requestTargets);
		incomingHandler.addHandler("inviteResponse", InviteResponse.class, handlers::inviteResponse);
		incomingHandler.addHandler("query", ServerQueryMessage.class, handlers::query);
	}
	
	ChatArea getMainChat() {
		return mainChat;
	}
	
	public ChatArea newChatRoom(String name) {
		int id = roomCounter.incrementAndGet();
		ChatArea room = new ChatArea(id, name);
		chats.put(id, room);
		return room;
	}
	
	public Map<Integer, ClientIO> getClients() {
		return Collections.unmodifiableMap(clients);
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
			client.sendToClient(new ServerErrorMessage(e.getMessage()));
		}
	}

	public void newClient(ClientIO cl) {
		logger.info("New client: " + cl);
		cl.setId(clientId.incrementAndGet());
		clients.put(cl.getId(), cl);
	}
	
	public void onDisconnected(ClientIO client) {
		logger.info("Client disconnected: " + client);
		games.values().stream().filter(game -> game.hasPlayer(client))
			.forEach(game -> game.send(new ClientDisconnectedMessage(client.getName(), game.getPlayers().indexOf(client))));
		clients.remove(client);
		getMainChat().remove(client);
		broadcast(new UserStatusMessage(client.getId(), client.getName(), Status.OFFLINE));
	}

	void broadcast(Message data) {
		clients.values().forEach(cl -> cl.sendToClient(data));
	}

	public void addGameFactory(String gameType, GameFactory factory) {
		this.gameFactories.put(gameType, factory);
	}

	public Map<String, GameFactory> getGameFactories() {
		return Collections.unmodifiableMap(gameFactories);
	}
	
	public ServerGame createGame(String parameter) {
		GameFactory suppl = gameFactories.get(parameter);
		if (suppl == null) {
			throw new IllegalArgumentException("No such game factory: " + parameter);
		}
		ServerGame game = suppl.newGame(this, gameId.incrementAndGet());
		this.games.put(game.getId(), game);
		return game;
	}
	
	public Map<Integer, ChatArea> getChats() {
		return new HashMap<>(chats);
	}
	
	public Map<Integer, ServerGame> getGames() {
		return new HashMap<>(games);
	}
	
	public ServerHandler<GameInvite> getInvites() {
		return invites;
	}

	public void addConnections(ConnectionHandler handler) {
		handler.start();
		this.handlers.add(handler);
	}

	public AtomicReference<ClientIO> getPlayAny() {
		return playAny;
	}

	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}
	
	public void stop() {
		for (ConnectionHandler handler : handlers) {
			try {
				handler.shutdown();
			} catch (Exception e) {
				logger.error("Error shutting down " + handler, e);
			}
		}
		this.scheduler.shutdown();
	}
	
}
