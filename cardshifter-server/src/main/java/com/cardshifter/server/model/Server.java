package com.cardshifter.server.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.cardshifter.api.*;
import com.cardshifter.core.Log4jAdapter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.both.InviteResponse;
import com.cardshifter.api.both.PlayerConfigMessage;
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
import com.cardshifter.core.game.ServerGame;
import com.cardshifter.core.messages.IncomingHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Handles different parts of the server operations, such as message handling, chat room, game creation, current games
 * @author Simon Forsberg
 *
 */

public class Server implements ClientServerInterface {
	private static final Logger	logger = LogManager.getLogger(Server.class);

	private final AtomicInteger clientId = new AtomicInteger(0);
	private final AtomicInteger roomCounter = new AtomicInteger(0);
	private final AtomicInteger gameId = new AtomicInteger(0);
	
	/**
	 * The IncomingHandler receives messages and passes them to the correct Handler
	 */
	private final IncomingHandler incomingHandler;
	private final CommandHandler commandHandler;
	
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
		this.incomingHandler = new IncomingHandler();
		this.commandHandler = new CommandHandler(this);
		this.scheduler = Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder().setNameFormat("ai-thread-%d").build());
		mainChat = this.newChatRoom("Main");
		
		Handlers handlers = new Handlers(this);
		
		/**
		 * Add a handler for each type of command, message, and method in Handlers
		 */
		incomingHandler.addHandler("login", LoginMessage.class, handlers::loginMessage);
		incomingHandler.addHandler("chat", ChatMessage.class, handlers::chat);
		incomingHandler.addHandler("startgame", StartGameRequest.class, handlers::play);
		incomingHandler.addHandler("inviteResponse", InviteResponse.class, handlers::inviteResponse);
		incomingHandler.addHandler("query", ServerQueryMessage.class, handlers::query);
		
		// Directly game-related
		incomingHandler.addHandler("use", UseAbilityMessage.class, handlers::useAbility);
		incomingHandler.addHandler("requestTargets", RequestTargetsMessage.class, handlers::requestTargets);
		incomingHandler.addHandler("playerconfig", PlayerConfigMessage.class, handlers::incomingConfig);
	}
	
	/**
	 * 
	 * @return This is the master chat room where all clients connecting are automatically added
	 */
	ChatArea getMainChat() {
		return mainChat;
	}
	
	/**
	 * Creates a ChatArea a given name and assigns an id
	 * 
	 * @param name The name of the chat room to create
	 * @return The room that was created
	 */
	public ChatArea newChatRoom(String name) {
		int id = roomCounter.incrementAndGet();
		ChatArea room = new ChatArea(id, name);
		chats.put(id, room);
		return room;
	}
	
	/**
	 * 
	 * @return A collection of the current clients
	 */
	public Map<Integer, ClientIO> getClients() {
		return Collections.unmodifiableMap(clients);
	}

	/**
	 * Set the user name of a client or fail
	 *
	 * @param client The client
	 * @param name The user name to set
	 * @throws UserNameAlreadyInUseException If name is already used by another client
	 * @throws InvalidUserNameException If name is not a valid user name as determined by isValidUserName
	 */
	public void trySetClientName(ClientIO client, UserName name) throws UserNameAlreadyInUseException {
		synchronized (this) {
			for (ClientIO other : clients.values()) {
				if (other.getName().equals(name)) {
					throw new UserNameAlreadyInUseException();
				}
			}

			client.setName(name.getString());
		}
	}
	
	/**
	 * 
	 * @return Returns the IncomingHandler for the Server
	 */
	public IncomingHandler getIncomingHandler() {
		return incomingHandler;
	}

	/**
	 * Passes the message to incomingHandler which will parse and perform it
	 * 
	 * @param client The client sending the message
	 * @param json The actual contents of the message
	 */
	@Override
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

	/**
	 * Puts the client in the clients collection
	 * 
	 * @param client The client object that will be connecting
	 */
	public void newClient(ClientIO client) {
		logger.info("New client: " + client);
		clients.put(client.getId(), client);
	}
	
	/**
	 * Removes client from the clients collection and broadcasts the event
	 * 
	 * @param client The client object that was disconnected
	 */
	@Override
	public void onDisconnected(ClientIO client) {
		logger.info("Client disconnected: " + client);
		games.values().stream().filter(game -> game.hasPlayer(client))
			.forEach(game -> game.send(new ClientDisconnectedMessage(client.getName(), game.getPlayers().indexOf(client))));
		clients.remove(client.getId());
		getMainChat().remove(client);
		broadcast(new UserStatusMessage(client.getId(), client.getName(), Status.OFFLINE));
	}

	/**
	 * Sends the message to each client in the clients collection
	 * 
	 * @param data The message to broadcast
	 */
	void broadcast(Message data) {
		clients.values().forEach(cl -> cl.sendToClient(data));
	}

	/**
	 * Puts the game factory into the gameFactories collection
	 * 
	 * @param gameType Name of the game type
	 * @param factory The GameFactory object
	 */
	public void addGameFactory(String gameType, GameFactory factory) {
		this.gameFactories.put(gameType, factory);
	}

	/**
	 * 
	 * @return A collection of the current game factories
	 */
	public Map<String, GameFactory> getGameFactories() {
		return Collections.unmodifiableMap(gameFactories);
	}
	
	/**
	 * Puts the created game into the games collection unless its factory is invalid
	 * 
	 * @param parameter the name of the game factory to use
	 * @return A reference to the game object
	 */
	public ServerGame createGame(String parameter) {
		GameFactory suppl = gameFactories.get(parameter);
		if (suppl == null) {
			throw new IllegalArgumentException("No such game factory: " + parameter);
		}
		ServerGame game = suppl.newGame(this, gameId.incrementAndGet());
		this.games.put(game.getId(), game);
		return game;
	}
	
	/**
	 * 
	 * @return The available ChatAreas
	 */
	public Map<Integer, ChatArea> getChats() {
		return new HashMap<>(chats);
	}
	
	/**
	 * 
	 * @return a new hash map that contains the contents of games
	 */
	public Map<Integer, ServerGame> getGames() {
		return new HashMap<>(games);
	}
	
	/**
	 * 
	 * @return The invites ServerHandler object
	 */
	public ServerHandler<GameInvite> getInvites() {
		return invites;
	}

	/**
	 * Adds the ConnectionHandler to the handlers set
	 * 
	 * @param handler the ConnectionHandler to add
	 */
	public void addConnections(ConnectionHandler handler) {
		handler.start();
		this.handlers.add(handler);
	}

	/**
	 * 
	 * @return This could be used for randomly pairing up clients
	 */
	public AtomicReference<ClientIO> getPlayAny() {
		return playAny;
	}

	/**
	 * 
	 * @return The scheduler object
	 */
	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}
	
	/**
	 * Closes all clients, shuts down all handlers, shuts down the scheduler
	 */
	public void stop() {
		for (ClientIO client : new ArrayList<>(clients.values())) {
			client.close();
		}

		for (ConnectionHandler handler : handlers) {
			try {
				handler.shutdown();
			} catch (Exception e) {
				logger.error("Error shutting down " + handler, e);
			}
		}
		this.scheduler.shutdown();
	}
	
	/**
	 * 
	 * @return The CommandHandler object
	 */
	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	@Override
	public void performIncoming(Message message, ClientIO client) {
		getIncomingHandler().perform(message, client);
	}

	@Override
	public int newClientId() {
		return clientId.incrementAndGet();
	}

    @Override
    public LogInterface getLogger() {
        return new Log4jAdapter();
    }

}
