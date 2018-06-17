package com.cardshifter.core.groovy

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent
import com.cardshifter.modapi.actions.ActionComponent
import com.cardshifter.modapi.actions.ECSAction
import com.cardshifter.modapi.actions.TargetableCheckEvent
import com.cardshifter.modapi.base.ComponentRetriever
import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.ECSSystem
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.base.PlayerComponent
import com.cardshifter.modapi.cards.CardComponent
import com.cardshifter.modapi.cards.Cards
import com.cardshifter.modapi.cards.DeckComponent
import com.cardshifter.modapi.cards.DiscardComponent
import com.cardshifter.modapi.cards.DrawStartCards
import com.cardshifter.modapi.cards.HandComponent
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.phase.PhaseController

class MulliganSingleCardsAction implements ECSSystem {
    public static final String ACTION_NAME = "Discard"
    private final PhaseController phases
    final int minTargets
    final int maxTargets

    MulliganSingleCardsAction(ECSGame game, int minTargets, int maxTargets) {
        phases = ComponentRetriever.singleton(game, PhaseController.class)
        this.minTargets = minTargets
        this.maxTargets = maxTargets
    }

    @Override
    void startGame(ECSGame game) {
        ComponentRetriever<ActionComponent> actions = ComponentRetriever.retreiverFor(ActionComponent.class)
        game.getEvents().registerHandlerAfter(this, TargetableCheckEvent.class, {e -> this.targetAllowed(e)})
        game.getEvents().registerHandlerAfter(this, ActionAllowedCheckEvent.class, {e -> this.actionAllowed(e)})

        Set<Entity> players = game.getEntitiesWithComponent(PlayerComponent.class);
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
        return new ECSAction(player, ACTION_NAME, {e -> true}, {e -> this.performAction(e)}).addTargetSet(minTargets, maxTargets);
    }

    private void actionAllowed(ActionAllowedCheckEvent event) {
        if (!event.getAction().getName().equals(ACTION_NAME)) {
            return;
        }
        if (phases.currentEntity != event.getPerformer()) {
            event.setAllowed(false)
        }
        if (event.getEntity() != event.getPerformer()) {
            event.setAllowed(false)
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
        chosenTargets.forEach({e -> this.switchCard(e)})
        phases.nextPhase()
    }

    private void switchCard(Entity e) {
        ComponentRetriever<CardComponent> cards = ComponentRetriever.retreiverFor(CardComponent.class)
        Entity owner = cards.get(e).getOwner()
        ZoneComponent targetZone = owner.getComponent(DiscardComponent.class)
        if (targetZone == null) {
            targetZone = owner.getComponent(DeckComponent.class);
        }
        cards.get(e).moveToBottom(targetZone)
        DrawStartCards.drawCard(owner);
    }
}
