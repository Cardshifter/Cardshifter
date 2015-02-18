package com.cardshifter.core.game;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.zomis.cardshifter.ecs.EntitySerialization;
import net.zomis.cardshifter.ecs.config.ConfigComponent;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.ai.FakeAIClientTCG;
import com.cardshifter.api.ClientIO;
import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.api.incoming.RequestTargetsMessage;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.outgoing.AvailableTargetsMessage;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.EntityRemoveMessage;
import com.cardshifter.api.outgoing.PlayerMessage;
import com.cardshifter.api.outgoing.ResetAvailableActionsMessage;
import com.cardshifter.api.outgoing.ServerErrorMessage;
import com.cardshifter.api.outgoing.UpdateMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.api.outgoing.ZoneChangeMessage;
import com.cardshifter.api.outgoing.ZoneMessage;
import com.cardshifter.core.replays.ReplayRecordSystem;
import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.Actions;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.ai.AIComponent;
import com.cardshifter.modapi.ai.AISystem;
import com.cardshifter.modapi.ai.CardshifterAI;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSGameState;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.base.PlayerEliminatedEvent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.ZoneChangeEvent;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.events.EntityRemoveEvent;
import com.cardshifter.modapi.events.GameOverEvent;
import com.cardshifter.modapi.resources.ResourceValueChange;
import com.cardshifter.modapi.resources.Resources;

/**
 * Extends ServerGame which is primarily a state manager.
 * This initializes the ECSGame and accepts the ECSMod.
 * 
 * @author Simon Forsberg
 */
public class TCGGame extends ServerGame {
	
	private static final Logger logger = LogManager.getLogger(TCGGame.class);
	private final ComponentRetriever<CardComponent> card = ComponentRetriever.retreiverFor(CardComponent.class);
	
	private ComponentRetriever<PlayerComponent> playerData = ComponentRetriever.retreiverFor(PlayerComponent.class);
	/**
	 * Supplied as an argument to the initialization method
	 */
	private final ECSMod mod;
	private final Supplier<ScheduledExecutorService> aiExecutor;
	private final String modName;
	
	/**
	 * 
	 * @param aiExecutor AI action scheduler
	 * @param name Mod name
	 * @param id The game id
	 * @param mod The mod that the game will run
	 */
	public TCGGame(Supplier<ScheduledExecutorService> aiExecutor, String name, int id, ECSMod mod) {
		super(id, new ECSGame());
		this.modName = name;
		this.aiExecutor = aiExecutor;
		this.mod = mod;
	}
	
	/**
	 * Gets the card entity for the card, then sends a ZoneChangeMessage to all clients.
	 * Checks if the player has knowledge of the zone, and sends the real card data if so
	 * 
	 * @param event The ZoneChangeEvent object
	 */
	private void zoneChange(ZoneChangeEvent event) {
		Entity cardEntity = event.getCard();
		int source = -1;
		if (event.getSource() != null) {
			source = event.getSource().getZoneId();
		}
		for (ClientIO io : this.getPlayers()) {
			Entity player = playerFor(io);
			boolean sourceKnown = false;
			if (event.getSource() != null) {
				sourceKnown = event.getSource().isKnownTo(player);
			}
			io.sendToClient(new ZoneChangeMessage(event.getCard().getId(), source, event.getDestination().getZoneId()));

			if (event.getDestination().isKnownTo(player) && !sourceKnown) {
				sendRealCardData(io, event.getDestination().getZoneId(), cardEntity);
			}
		}
	}
	
	/**
	 * Sends a CardInfoMessage to the targeted player.
	 * This is sent in addition to zoneChange for zones the player has knowledge of
	 * 
	 * @param io Target client
	 * @param zoneId Zone that the card is in
	 * @param cardEntity The card Entity object
	 */
	private void sendRealCardData(ClientIO io, int zoneId, Entity cardEntity) {
		io.sendToClient(new CardInfoMessage(zoneId, cardEntity.getId(), infoMap(cardEntity)));
	}

	/**
	 * Sends an EntityRemoveMessage for the supplied event
	 * 
	 * @param event The EntityRemove event
	 */
	private void remove(EntityRemoveEvent event) {
		this.send(new EntityRemoveMessage(event.getEntity().getId()));
	}
	
	/**
	 * If the event is a for a player, zone, or game, an UpdateMessage is sent to all players.
	 * For cards it is only sent to players who have knowledge of the card zone
	 * 
	 * @param event The ResourceValueChange event
	 */
	private void broadcast(ResourceValueChange event) {
		if (game.getGameState() == ECSGameState.NOT_STARTED) {
			// let the most information be sent when actually starting the game
			return;
		}
		
		Entity entity = event.getEntity();
		UpdateMessage updateEvent = new UpdateMessage(entity.getId(), event.getResource().toString(), event.getNewValue());
		
		if (card.has(entity)) {
			CardComponent cardData = card.get(entity);
			for (ClientIO io : this.getPlayers()) {
				Entity player = playerFor(io);
				if (cardData.getCurrentZone().isKnownTo(player)) {
					io.sendToClient(updateEvent);
				}
			}
		}
		else {
			// Player, Zone, or Game
			this.send(updateEvent);
		}
	}
	
	/**
	 * Sends a list of target entities based on the action supplied in the message
	 * 
	 * @param message The RequestTargetsMessage object
	 * @param client The client requesting the targets
	 */
	public void informAboutTargets(RequestTargetsMessage message, ClientIO client) {
		ECSAction action = findAction(message.getId(), message.getAction());
		TargetSet targetAction = action.getTargetSets().get(0);
		List<Entity> targets = targetAction.findPossibleTargets();
		int[] targetIds = targets.stream().mapToInt(e -> e.getId()).toArray();
		
		client.sendToClient(new AvailableTargetsMessage(message.getId(), message.getAction(), targetIds, targetAction.getMin(), targetAction.getMax()));
	}
	
	/**
	 * Look for a specific action on a specific entity
	 * 
	 * @param entityId Target entity to search for actions
	 * @param actionId Action to look for on the entity
	 * @return The ECSAction object
	 */
	public ECSAction findAction(int entityId, String actionId) {
		Entity entity = Objects.requireNonNull(game.getEntity(entityId), "Entity " + entityId + " not found");
		ECSAction action = Actions.getAction(entity, actionId);
		return Objects.requireNonNull(action, "Action " + actionId + " not found on entity " + entityId);
	}
	
	/**
	 * Checks the game state, client, and action for validity.
	 * If valid, finds the action of the message, finds the available targets, and sends them to the client
	 * 
	 * @param message The UseAbilityMessage object
	 * @param client The client object who sent the move
	 */
	public void handleMove(UseAbilityMessage message, ClientIO client) {
		if (this.isGameOver()) {
			logger.info("Ignoring move because game has ended: " + message + " from " + client);
			return;
		}
		
		if (!this.getPlayers().contains(client)) {
			throw new IllegalArgumentException("Client is not in this game: " + client);
		}
		
		ECSAction action = findAction(message.getId(), message.getAction());
		if (!action.getTargetSets().isEmpty()) {
			TargetSet targetAction = action.getTargetSets().get(0);
			targetAction.clearTargets();
			for (int target : message.getTargets()) {
				targetAction.addTarget(game.getEntity(target));
			}
		}
		boolean allowed = action.perform(playerFor(client));
		if (!allowed) {
			client.sendToClient(new ServerErrorMessage("Action not allowed: " + action));
		}
		
		sendAvailableActions();
	}
	
	/**
	 * 
	 * @param io The target client
	 * @return The index of the client in this object
	 */
	public Entity playerFor(ClientIO io) {
		int index = this.getPlayers().indexOf(io);
		if (index < 0) {
			throw new IllegalArgumentException(io + " is not a valid player in this game");
		}
		return getPlayer(index);
	}
	
	/**
	 * 
	 * @param index The index to search for
	 * @return The player at that index
	 */
	private Entity getPlayer(int index) {
		List<Entity> players = game.findEntities(entity -> entity.hasComponent(PlayerComponent.class) && entity.getComponent(PlayerComponent.class).getIndex() == index);
		if (players.size() != 1) {
			throw new IllegalStateException("Found " + players.size() + " results for entities with Player index " + index);
		}
		return players.get(0);
	}
	
	/**
	 * Initializes configurations, starts the ECSGame, and sets up the AI
	 */
	@Override
	protected void onStart() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss").withZone(ZoneId.systemDefault());
		String time = formatter.format(Instant.now());
		File directory = new File("replays", modName);
		directory.mkdirs();
		game.addSystem(new ReplayRecordSystem(game, modName, new File(directory, "replay-" + getId() + "-" + time + ".json")));
		
		if (!preStartForConfiguration()) {
			this.startECSGame();
			this.setupAIPlayers(); // PlayerComponents needs to be setup first
			AISystem.call(game);
		}
	}
	
	/**
	 * Pre-start the game to ask for configuration 
	 * 
	 * @return
	 */
	public boolean preStartForConfiguration() {
		mod.declareConfiguration(game);
		
		if (this.isConfigNeeded()) {
			this.setupAIPlayers();
			this.requestPlayerConfig();
			return true;
		}
		return false;
	}
	
	/**
	 * Listener for when a player is eliminated from the game
	 * @param event Event about the elimination
	 */
	private void playerEliminated(PlayerEliminatedEvent event) {
		String winStatus = event.isDeclaredWinner() ? "won" : "lost";
		PlayerComponent player = event.getEntity().getComponent(PlayerComponent.class);
		this.sendChat(player.getName() + " " + winStatus + " game " + getId());		
	}
	
	public void sendChat(String message) {
		this.send(new ChatMessage(0, "Server", message));
	}
	
	/**
	 * Sets up the game based on the mod.
	 * Events are registered to the game which handle a class and call a method.
	 * Sends the initial available actions for the game.
	 */
	private void startECSGame() {
		mod.setupGame(game);
		
		game.getEvents().registerHandlerAfter(this, ResourceValueChange.class, this::broadcast);
		game.getEvents().registerHandlerAfter(this, ZoneChangeEvent.class, this::zoneChange);
		game.getEvents().registerHandlerAfter(this, EntityRemoveEvent.class, this::remove);
		game.getEvents().registerHandlerAfter(this, PlayerEliminatedEvent.class, this::playerEliminated);
		game.getEvents().registerHandlerAfter(this, GameOverEvent.class, event -> this.endGame());
		AISystem.setup(game, aiExecutor.get());
		game.addSystem(game -> game.getEvents().registerHandlerAfter(this, ActionPerformEvent.class, event -> this.sendAvailableActions()));
		
		game.startGame();
		this.getPlayers().stream().forEach(pl -> {
			Entity playerEntity = playerFor(pl);
			PlayerComponent plData = playerEntity.get(playerData);
			plData.setName(pl.getName());
			this.send(new PlayerMessage(playerEntity.getId(), plData.getIndex(), plData.getName(), Resources.map(playerEntity)));
		});
		this.game.findEntities(e -> true).stream().flatMap(e -> e.getSuperComponents(ZoneComponent.class).stream()).forEach(this::sendZone);
		this.sendAvailableActions();
	}

	/**
	 * Sends a request to players to setup player-specific configuration (special powers, decks, etc.)
	 * 
	 * @return True if a request for player-specific configuration has been sent, false if no additional configuration is required.
	 */
	private boolean requestPlayerConfig() {
		Set<Entity> configEntities = game.getEntitiesWithComponent(ConfigComponent.class);
		boolean sent = false;
		for (ClientIO io : getPlayers()) {
			Entity playerEntity = playerFor(io);
			if (configEntities.contains(playerEntity)) {
				PlayerConfigMessage configMessage = new PlayerConfigMessage(getId(), playerEntity.getComponent(ConfigComponent.class).getConfigs());
				io.sendToClient(configMessage);
				if (io instanceof FakeAIClientTCG) {
					FakeAIClientTCG aiClient = (FakeAIClientTCG) io;
					CardshifterAI ai = aiClient.getAI();
					ai.configure(playerEntity, playerEntity.getComponent(ConfigComponent.class));
					playerEntity.getComponent(ConfigComponent.class).setConfigured(true);
				}
				else {
					sent = true;
				}
			}
		}
		
		return sent;
	}

	/**
	 * If a ClientIO in the game is an AI Client, the AI is initialized to its preset AI level
	 */
	private void setupAIPlayers() {
		for (ClientIO io : this.getPlayers()) {
			if (io instanceof FakeAIClientTCG) {
				FakeAIClientTCG aiClient = (FakeAIClientTCG) io;
				Entity player = playerFor(io);
				AIComponent aiComponent = new AIComponent(aiClient.getAI());
				aiComponent.setDelay(2000);
				player.addComponent(aiComponent);
				logger.info("AI is configured for " + player);
			}
		}
	}

	/**
	 * Looks at all Clients, resets their actions, then gets all actions for each and sends them.
	 */
	private void sendAvailableActions() {
		for (ClientIO io : this.getPlayers()) {
			io.sendToClient(new ResetAvailableActionsMessage());
			if (game.isGameOver()) {
				continue;
			}
			Entity player = playerFor(io);
			getAllActions(game).filter(action -> action.isAllowed(player))
				.forEach(action -> io.sendToClient(new UsableActionMessage(action.getOwner().getId(), action.getName(), !action.getTargetSets().isEmpty())));
		}
	}

	/**
	 * 
	 * @param game The game to search for actions
	 * @return A stream of action components for all entities in the game
	 */
	private static Stream<ECSAction> getAllActions(ECSGame game) {
		return game.getEntitiesWithComponent(ActionComponent.class)
			.stream()
			.flatMap(entity -> entity.getComponent(ActionComponent.class)
					.getECSActions().stream());
	}
	
	/**
	 * Sends the zone to all players. If the zone is known, also sends the cards.
	 * 
	 * @param zone The zone component to send
	 */
	private void sendZone(ZoneComponent zone) {
		for (ClientIO io : this.getPlayers()) {
			Entity player = playerFor(io);
			io.sendToClient(constructZoneMessage(zone, player));
			if (zone.isKnownTo(player)) {
				zone.forEach(card -> this.sendCard(io, card));
			}
		}
	}
	
	/**
	 * Zones are treated differently when they are known to the target player.
	 * 
	 * @param zone The zone component object
	 * @param player The target player for the message
	 * @return A zone message constructed based on the zone component object properties.
	 */
	private ZoneMessage constructZoneMessage(ZoneComponent zone, Entity player) {
		return new ZoneMessage(zone.getZoneId(), zone.getName(), 
				zone.getOwner().getId(), zone.size(), zone.isKnownTo(player), zone.stream().mapToInt(e -> e.getId()).toArray());
	}
	
	/**
	 * A CardInfoMesage is created and sent to the target client.
	 * 
	 * @param io The target client
	 * @param card The card entity to send
	 */
	private void sendCard(ClientIO io, Entity card) {
		CardComponent cardData = card.getComponent(CardComponent.class);
		io.sendToClient(new CardInfoMessage(cardData.getCurrentZone().getZoneId(), card.getId(), infoMap(card)));
	}
	
	/**
	 * 
	 * @param entity The entity to map
	 * @return A map of the target entity
	 */
	private Map<String, Object> infoMap(Entity entity) {
		return EntitySerialization.serialize(entity);
	}

	/**  
	 * Set the player configuration for a player and then if no more configuration is needed, start the ECSGame.
	 * 
	 * @param message The PlayerConfigMessage object
	 * @param client The client that sent the config
	 */
	public void incomingPlayerConfig(PlayerConfigMessage message, ClientIO client) {
		Entity player = playerFor(client);
		ConfigComponent config = player.getComponent(ConfigComponent.class);
		for (Entry<String, Object> entry : message.getConfigs().entrySet()) {
			config.addConfig(entry.getKey(), entry.getValue());
			logger.info("Incoming player config for " + player + ": " + entry.getValue());
		}
		config.setConfigured(true);
		checkStartGame();
	}

	public void checkStartGame() {
		if (!this.isConfigNeeded()) {
			startECSGame();
		}
	}

	/**
	 * 
	 * @return Returns true if any of the ConfigComponents are not configured
	 */
	public boolean isConfigNeeded() {
		Set<Entity> configEntities = game.getEntitiesWithComponent(ConfigComponent.class);
		return configEntities.stream().map(e -> e.getComponent(ConfigComponent.class)).anyMatch(config -> !config.isConfigured());
	}
	
}