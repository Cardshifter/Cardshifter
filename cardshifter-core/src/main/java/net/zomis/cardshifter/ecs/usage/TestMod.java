package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.api.config.DeckConfig;
import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.UseCostSystem;
import com.cardshifter.modapi.actions.attack.*;
import com.cardshifter.modapi.actions.enchant.EnchantPerform;
import com.cardshifter.modapi.actions.enchant.EnchantTargetCreatureTypes;
import com.cardshifter.modapi.attributes.Attributes;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.*;
import com.cardshifter.modapi.cards.*;
import com.cardshifter.modapi.phase.*;
import com.cardshifter.modapi.players.Players;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.GameOverIfNoHealth;
import com.cardshifter.modapi.resources.ResourceRetriever;
import com.cardshifter.modapi.resources.RestoreResourcesToSystem;
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import net.zomis.cardshifter.ecs.config.DeckConfigFactory;
import net.zomis.cardshifter.ecs.effects.EffectActionSystem;
import net.zomis.cardshifter.ecs.effects.EffectTargetFilterSystem;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

/**
 * Created by Simon on 2/12/2015.
 */
public class TestMod implements ECSMod {

    public static final String END_TURN_ACTION = "End Turn";

    @Override
    public void setupGame(ECSGame game) {
        PhaseController phaseController = new PhaseController();
        game.newEntity().addComponent(phaseController);

        for (int i = 0; i < 2; i++) {
            final int playerIndex = i;
            Entity player = game.newEntity();
            PlayerComponent playerComponent = new PlayerComponent(i, "Player" + (i+1));
            player.addComponent(playerComponent);

            Phase playerPhase = new Phase(player, "Main");
            phaseController.addPhase(playerPhase);

            ActionComponent actions = new ActionComponent();
            player.addComponent(actions);

            ECSAction endTurnAction = new ECSAction(player, END_TURN_ACTION, act -> phaseController.getCurrentPhase() == playerPhase, act -> phaseController.nextPhase());
            actions.addAction(endTurnAction);

            ECSResourceMap.createFor(player)
                    .set(PhrancisGame.PhrancisResources.HEALTH, 30)
                    .set(PhrancisGame.PhrancisResources.MAX_HEALTH, 30);

            ZoneComponent deck = new DeckComponent(player);
            ZoneComponent hand = new HandComponent(player);
            ZoneComponent battlefield = new BattlefieldComponent(player);
            createCards(hand);
            createCards(deck);
            createCards(deck);
            createCards(deck);
            createCards(deck);
            player.addComponents(hand, deck, battlefield);

            deck.shuffle();
        }

        // Draw cards
        game.addSystem(new LimitedHandSizeSystem(10, card -> card.getCardToDraw().destroy()));
        game.addSystem(new DrawCardAtBeginningOfTurnSystem());
        game.addSystem(new LimitedActionsPerTurnSystem(10, END_TURN_ACTION));

        // General setup
        game.addSystem(new LastPlayersStandingEndsGame());
        game.addSystem(new RemoveDeadEntityFromZoneSystem());
        game.addSystem(new PerformerMustBeCurrentPlayer());
    }

    private void createCards(ZoneComponent hand) {
        for (int i = 0; i < 5; i++) {
            Entity entity = hand.getOwner().getGame().newEntity();
            ECSResourceMap.createFor(entity)
                    .set(PhrancisGame.PhrancisResources.HEALTH, 3)
                    .set(PhrancisGame.PhrancisResources.MAX_HEALTH, 3);
            ECSAttributeMap.createFor(entity).set(Attributes.NAME, "Test");
            ActionComponent action = new ActionComponent();
            entity.addComponent(action);
            action.addAction(moveAction("Field", entity, BattlefieldComponent.class, false));
            action.addAction(moveAction("Hand", entity, HandComponent.class, false));
            action.addAction(moveAction("Deck", entity, DeckComponent.class, false));

            action.addAction(moveAction("2-Field", entity, BattlefieldComponent.class, true));
            action.addAction(moveAction("2-Hand", entity, HandComponent.class, true));
            action.addAction(moveAction("2-Deck", entity, DeckComponent.class, true));
            action.addAction(damageAction(entity));
            hand.addOnBottom(entity);
        }
    }

    private ECSAction damageAction(Entity entity) {
        ResourceRetriever health = ResourceRetriever.forResource(PhrancisGame.PhrancisResources.HEALTH);
        return new ECSAction(entity, "Damage", act -> true, act -> {
            health.resFor(entity).change(-1);
            if (health.getFor(entity) == 0) {
                entity.destroy();
            }
        });
    }

    private ECSAction moveAction(String name, Entity entity, Class<? extends ZoneComponent> zone, boolean switchPlayer) {
        return new ECSAction(entity, name, act -> true, act -> {
            CardComponent card = act.getOwner().getComponent(CardComponent.class);
            Entity player = Players.findOwnerFor(act.getOwner());
            final Entity origPlayer = player;
            if (switchPlayer) {
                Set<Entity> players = entity.getGame().getEntitiesWithComponent(PlayerComponent.class);
                player = players.stream().filter(pl -> pl != origPlayer).findAny().get();
            }
            card.moveToBottom(player.getComponent(zone));
        });
    }

}
