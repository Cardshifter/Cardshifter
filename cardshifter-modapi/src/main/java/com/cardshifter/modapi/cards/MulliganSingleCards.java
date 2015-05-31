package com.cardshifter.modapi.cards;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.TargetableCheckEvent;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.phase.Phase;
import com.cardshifter.modapi.phase.PhaseController;

public class MulliganSingleCards implements ECSSystem {

	private static final String PHASE_NAME = "Mulligan";
	private static final String ACTION_NAME = "Mulligan";
	
	private final PhaseController phases;
	private final AtomicInteger remainingPerforms = new AtomicInteger(0);

	public MulliganSingleCards(ECSGame game) {
		phases = ComponentRetriever.singleton(game, PhaseController.class);
		phases.insertTemporaryPhaseBeforeCurrent(new Phase(null, PHASE_NAME));
	}
	
	@Override
	public void startGame(ECSGame game) {
		ComponentRetriever<ActionComponent> actions = ComponentRetriever.retreiverFor(ActionComponent.class);
		game.getEvents().registerHandlerAfter(this, TargetableCheckEvent.class, this::targetAllowed);
		game.getEvents().registerHandlerAfter(this, ActionAllowedCheckEvent.class, this::actionAllowed);
		
		Set<Entity> players = game.getEntitiesWithComponent(PlayerComponent.class);
		remainingPerforms.set(players.size());
		for (Entity player : players) {
			ActionComponent playerActions = player.get(actions);
			if (playerActions == null) {
				playerActions = new ActionComponent();
				player.addComponent(playerActions);
			}
			playerActions.addAction(createMulliganAction(player));
		}
	}

	private ECSAction createMulliganAction(Entity player) {
		return new ECSAction(player, ACTION_NAME, act -> true, this::performAction).addTargetSet(0, Integer.MAX_VALUE);
	}
	
	private void actionAllowed(ActionAllowedCheckEvent event) {
		if (!event.getAction().getName().equals(ACTION_NAME)) {
			return;
		}
		if (event.getEntity() != event.getPerformer()) {
			event.setAllowed(false);
		}
		if (!PHASE_NAME.equals(phases.getCurrentPhase().getName())) {
			event.setAllowed(false);
		}
	}
	
	private void targetAllowed(TargetableCheckEvent event) {
		if (!event.getAction().getName().equals(ACTION_NAME)) {
			return;
		}
		if (!event.getTarget().hasComponent(CardComponent.class)) {
			event.setAllowed(false);
			return;
		}
		if (!Cards.isOnZone(event.getTarget(), HandComponent.class)) {
			event.setAllowed(false);
		}
		if (!Cards.isOwnedBy(event.getTarget(), event.getAction().getOwner())) {
			event.setAllowed(false);
		}
	}
	
	private void performAction(ECSAction mulliganAction) {
		List<Entity> chosenTargets = mulliganAction.getTargetSets().get(0).getChosenTargets();
		chosenTargets.forEach(this::switchCard);
        Entity owner = mulliganAction.getOwner();
		owner.getComponent(ActionComponent.class).removeAction(ACTION_NAME);
        owner.getComponent(DeckComponent.class).shuffle();
		if (remainingPerforms.decrementAndGet() == 0) {
			phases.nextPhase();
		}
	}
	
	private void switchCard(Entity e) {
		ComponentRetriever<CardComponent> cards = ComponentRetriever.retreiverFor(CardComponent.class);
		Entity owner = cards.get(e).getOwner();
		DeckComponent deck = owner.getComponent(DeckComponent.class);
		cards.get(e).moveToBottom(deck);
		DrawStartCards.drawCard(owner);
	}

}
