package com.cardshifter.server.game;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.zomis.cardshifter.ecs.EntitySerialization;
import net.zomis.cardshifter.ecs.usage.ConfigComponent;
import net.zomis.cardshifter.ecs.usage.DeckConfig;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.ClientIO;
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
import com.cardshifter.api.outgoing.UseableActionMessage;
import com.cardshifter.api.outgoing.ZoneChangeMessage;
import com.cardshifter.api.outgoing.ZoneMessage;
import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.Actions;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.ai.AIComponent;
import com.cardshifter.modapi.ai.AISystem;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.ZoneChangeEvent;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.events.EntityRemoveEvent;
import com.cardshifter.modapi.events.GameOverEvent;
import com.cardshifter.modapi.resources.ResourceValueChange;
import com.cardshifter.modapi.resources.Resources;
import com.cardshifter.server.main.FakeAIClientTCG;
import com.cardshifter.server.model.GameState;
import com.cardshifter.server.model.ServerGame;

public class TCGGame extends ServerGame {
	
	private static final Logger logger = LogManager.getLogger(TCGGame.class);
	private final ComponentRetriever<CardComponent> card = ComponentRetriever.retreiverFor(CardComponent.class);
	
	private ComponentRetriever<PlayerComponent> playerData = ComponentRetriever.retreiverFor(PlayerComponent.class);
	private final ECSMod mod;
	private final Supplier<ScheduledExecutorService> aiExecutor;
	
	public TCGGame(Supplier<ScheduledExecutorService> aiExecutor, int id, ECSMod mod) {
		super(id, new ECSGame());
		this.aiExecutor = aiExecutor;
		this.mod = mod;
	}

	private void zoneChange(ZoneChangeEvent event) {
		Entity cardEntity = event.getCard();
		for (ClientIO io : this.getPlayers()) {
			Entity player = playerFor(io);
			io.sendToClient(new ZoneChangeMessage(event.getCard().getId(), event.getSource().getZoneId(), event.getDestination().getZoneId()));
			if (event.getDestination().isKnownTo(player) && !event.getSource().isKnownTo(player)) {
				sendRealCardData(io, event.getDestination().getZoneId(), cardEntity);
			}
		}
	}
	
	private void sendRealCardData(ClientIO io, int zoneId, Entity cardEntity) {
		io.sendToClient(new CardInfoMessage(zoneId, cardEntity.getId(), infoMap(cardEntity)));
	}

	private void remove(EntityRemoveEvent event) {
		this.send(new EntityRemoveMessage(event.getEntity().getId()));
	}
	
	private void broadcast(ResourceValueChange event) {
		if (getState() == GameState.NOT_STARTED) {
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
	
	public void informAboutTargets(RequestTargetsMessage message, ClientIO client) {
		ECSAction action = findAction(message.getId(), message.getAction());
		TargetSet targetAction = action.getTargetSets().get(0);
		List<Entity> targets = targetAction.findPossibleTargets();
		int[] targetIds = targets.stream().mapToInt(e -> e.getId()).toArray();
		
		client.sendToClient(new AvailableTargetsMessage(message.getId(), message.getAction(), targetIds, targetAction.getMin(), targetAction.getMax()));
	}
	
	public ECSAction findAction(int entityId, String actionId) {
		Entity entity = Objects.requireNonNull(game.getEntity(entityId), "Entity " + entityId + " not found");
		ECSAction action = Actions.getAction(entity, actionId);
		return Objects.requireNonNull(action, "Action " + actionId + " not found on entity " + entityId);
	}
	
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
	
	public Entity playerFor(ClientIO io) {
		int index = this.getPlayers().indexOf(io);
		if (index < 0) {
			throw new IllegalArgumentException(io + " is not a valid player in this game");
		}
		return getPlayer(index);
	}
	
	private Entity getPlayer(int index) {
		List<Entity> players = game.findEntities(entity -> entity.hasComponent(PlayerComponent.class) && entity.getComponent(PlayerComponent.class).getIndex() == index);
		if (players.size() != 1) {
			throw new IllegalStateException("Found " + players.size() + " results for entities with Player index " + index);
		}
		return players.get(0);
	}
	
	@Override
	protected void onStart() {
		mod.declareConfiguration(game);
		
		if (this.isConfigNeeded()) {
			this.setupAIPlayers();
			this.requestPlayerConfig();
			return;
		}
		this.startECSGame();
		this.setupAIPlayers();
	}
	
	private void startECSGame() {
		mod.setupGame(game);
		
		game.getEvents().registerHandlerAfter(this, ResourceValueChange.class, this::broadcast);
		game.getEvents().registerHandlerAfter(this, ZoneChangeEvent.class, this::zoneChange);
		game.getEvents().registerHandlerAfter(this, EntityRemoveEvent.class, this::remove);
		game.getEvents().registerHandlerAfter(this, GameOverEvent.class, event -> this.endGame());
		AISystem.setup(game, aiExecutor.get());
		game.addSystem(game -> game.getEvents().registerHandlerAfter(this, ActionPerformEvent.class, event -> this.sendAvailableActions()));
		
		game.startGame();
		this.getPlayers().stream().forEach(pl -> {
			Entity playerEntity = playerFor(pl);
			PlayerComponent plData = playerEntity.get(playerData);
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
					generateRandomDeck(io, configMessage);
				}
				else {
					sent = true;
				}
			}
		}
		
		return sent;
	}

	private void generateRandomDeck(ClientIO client, PlayerConfigMessage configMessage) {
		Map<String, Object> configs = configMessage.getConfigs();
		for (Entry<String, Object> entry : configs.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof DeckConfig) {
				DeckConfig deckConfig = (DeckConfig) value;
				deckConfig.generateRandom();
			}
		}
		PlayerConfigMessage finalConfig = new PlayerConfigMessage(configMessage.getGameId(), configs);
		this.incomingPlayerConfig(finalConfig, client);
	}

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

	private void sendAvailableActions() {
		for (ClientIO io : this.getPlayers()) {
			io.sendToClient(new ResetAvailableActionsMessage());
			if (game.isGameOver()) {
				continue;
			}
			Entity player = playerFor(io);
			getAllActions(game).filter(action -> action.isAllowed(player))
				.forEach(action -> io.sendToClient(new UseableActionMessage(action.getOwner().getId(), action.getName(), !action.getTargetSets().isEmpty())));
		}
	}

	private static Stream<ECSAction> getAllActions(ECSGame game) {
		return game.getEntitiesWithComponent(ActionComponent.class)
			.stream()
			.flatMap(entity -> entity.getComponent(ActionComponent.class)
					.getECSActions().stream());
	}
	
	private void sendZone(ZoneComponent zone) {
		for (ClientIO io : this.getPlayers()) {
			Entity player = playerFor(io);
			io.sendToClient(constructZoneMessage(zone, player));
			if (zone.isKnownTo(player)) {
				zone.forEach(card -> this.sendCard(io, card));
			}
		}
	}
	
	private ZoneMessage constructZoneMessage(ZoneComponent zone, Entity player) {
		return new ZoneMessage(zone.getZoneId(), zone.getName(), 
				zone.getOwner().getId(), zone.size(), zone.isKnownTo(player), zone.stream().mapToInt(e -> e.getId()).toArray());
	}
	
	private void sendCard(ClientIO io, Entity card) {
		CardComponent cardData = card.getComponent(CardComponent.class);
		io.sendToClient(new CardInfoMessage(cardData.getCurrentZone().getZoneId(), card.getId(), infoMap(card)));
	}
	
	private Map<String, Object> infoMap(Entity entity) {
		return EntitySerialization.serialize(entity);
	}

	public void incomingPlayerConfig(PlayerConfigMessage message, ClientIO client) {
		Entity player = playerFor(client);
		ConfigComponent config = player.getComponent(ConfigComponent.class);
		for (Entry<String, Object> entry : message.getConfigs().entrySet()) {
			config.addConfig(entry.getKey(), entry.getValue());
			logger.info("Incoming player config for " + player + ": " + entry.getValue());
		}
		config.setConfigured(true);
		if (!this.isConfigNeeded()) {
			startECSGame();
		}
	}

	private boolean isConfigNeeded() {
		Set<Entity> configEntities = game.getEntitiesWithComponent(ConfigComponent.class);
		return configEntities.stream().map(e -> e.getComponent(ConfigComponent.class)).anyMatch(config -> !config.isConfigured());
	}
	
}
