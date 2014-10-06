package com.cardshifter.server.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import net.zomis.cardshifter.ecs.actions.ActionComponent;
import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.actions.TargetSet;
import net.zomis.cardshifter.ecs.ai.AIComponent;
import net.zomis.cardshifter.ecs.ai.AISystem;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.EntityRemoveEvent;
import net.zomis.cardshifter.ecs.base.GameOverEvent;
import net.zomis.cardshifter.ecs.cards.CardComponent;
import net.zomis.cardshifter.ecs.cards.ZoneChangeEvent;
import net.zomis.cardshifter.ecs.cards.ZoneComponent;
import net.zomis.cardshifter.ecs.components.CreatureTypeComponent;
import net.zomis.cardshifter.ecs.components.PlayerComponent;
import net.zomis.cardshifter.ecs.resources.ResourceValueChange;
import net.zomis.cardshifter.ecs.resources.Resources;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
import com.cardshifter.server.main.FakeAIClientTCG;

public class TCGGame extends ServerGame {
	
	private static final Logger logger = LogManager.getLogger(TCGGame.class);
	private final ECSGame game;
	private final ComponentRetriever<CardComponent> card = ComponentRetriever.retreiverFor(CardComponent.class);
	private final ComponentRetriever<CreatureTypeComponent> creatureType = ComponentRetriever.retreiverFor(CreatureTypeComponent.class);
	
	private ComponentRetriever<PlayerComponent> playerData = ComponentRetriever.retreiverFor(PlayerComponent.class);
	
	public TCGGame(Server server, int id) {
		super(server, id);
		game = PhrancisGame.createGame(new ECSGame());
		game.getEvents().registerHandlerAfter(this, ResourceValueChange.class, this::broadcast);
		game.getEvents().registerHandlerAfter(this, ZoneChangeEvent.class, this::zoneChange);
		game.getEvents().registerHandlerAfter(this, EntityRemoveEvent.class, this::remove);
		game.getEvents().registerHandlerAfter(this, GameOverEvent.class, event -> this.endGame());
		AISystem.setup(game, server.getScheduler());
		game.addSystem(game -> game.getEvents().registerHandlerAfter(this, ActionPerformEvent.class, event -> this.sendAvailableActions()));
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
	
	public Entity findTargetable(int entityId) {
		Optional<Entity> entity = game.findEntities(e -> e.getId() == entityId).stream().findFirst();
		return entity.orElse(null);
	}
	
	public ECSAction findAction(int entityId, String actionId) {
		Optional<Entity> entity = game.findEntities(e -> e.getId() == entityId).stream().findFirst();
		
		if (!entity.isPresent()) {
			throw new IllegalArgumentException("No such entity found");
		}
		Entity e = entity.get();
		if (e.hasComponent(ActionComponent.class)) {
			ActionComponent comp = e.getComponent(ActionComponent.class);
			if (comp.getActions().contains(actionId)) {
				return comp.getAction(actionId);
			}
			throw new IllegalArgumentException("No such action was found.");
		}
		throw new IllegalArgumentException(e + " does not have an action component");
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
				targetAction.addTarget(findTargetable(target));
			}
		}
		boolean allowed = action.perform(playerFor(client));
		if (!allowed) {
			client.sendToClient(new ServerErrorMessage("Action not allowed: " + action));
		}
		
		// TODO: Add listener to game for ZoneMoves, inform players about card movements, and send CardInfoMessage when a card becomes known
		sendAvailableActions();
	}
	
	@Override
	protected boolean makeMove(Command command, int player) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void updateStatus() {
		
	}

	public Entity playerFor(ClientIO io) {
		int index = this.getPlayers().indexOf(io);
		if (index < 0) {
			throw new IllegalArgumentException(io + " is not a valid player in this game");
		}
		return getPlayer(index);
	}
	
	private Entity getPlayer(int index) {
		return game.findEntities(entity -> entity.hasComponent(PlayerComponent.class) && entity.getComponent(PlayerComponent.class).getIndex() == index).get(0);
	}
	
	@Override
	protected void onStart() {
		this.setupAIPlayers();
		
		game.startGame();
		this.getPlayers().stream().forEach(pl -> {
			Entity playerEntity = playerFor(pl);
			PlayerComponent plData = playerEntity.get(playerData);
			this.send(new PlayerMessage(playerEntity.getId(), plData.getIndex(), plData.getName(), Resources.map(playerEntity)));
		});
		this.game.findEntities(e -> true).stream().flatMap(e -> e.getSuperComponents(ZoneComponent.class).stream()).forEach(this::sendZone);
		this.sendAvailableActions();
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
			Entity player = playerFor(io);
			io.sendToClient(new ResetAvailableActionsMessage());
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
		Map<String, Object> result = new HashMap<>();
		result.putAll(Resources.map(entity));
		if (creatureType.has(entity)) {
			result.put("creatureType", creatureType.get(entity).getCreatureType());
		}
		return result;
	}

	@Override
	public ECSGame getGameModel() {
		return game;
	}
	
}
