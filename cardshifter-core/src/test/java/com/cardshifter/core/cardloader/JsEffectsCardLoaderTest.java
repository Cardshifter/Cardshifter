package com.cardshifter.core.cardloader;

import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.ModDSL;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.base.Retrievers;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.events.IEvent;
import com.cardshifter.modapi.phase.Phase;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.phase.PhaseEndEvent;
import com.cardshifter.modapi.players.Players;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.ResourceRetriever;
import net.zomis.cardshifter.ecs.effects.Filters;
import net.zomis.cardshifter.ecs.effects.TargetFilter;
import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class JsEffectsCardLoaderTest {
    @Test
    public void testLoadCards() throws URISyntaxException, CardLoadingException {
        ECSGame game = new ECSGame();
        CustomMod customMod = new CustomMod();
        customMod.declareConfiguration(game);
        customMod.setupGame(game);

        Entity ragnaros = game.getEntitiesWithComponent(CardComponent.class).stream()
            .filter(entity -> AttributeRetriever.forAttribute(CustomAttributes.NAME).getOrDefault(entity, "").equals("Ragnaros"))
            .findFirst().get();
        assertEquals(8, ResourceRetriever.forResource(CustomResources.ATTACK).getFor(ragnaros));
        assertEquals(8, ResourceRetriever.forResource(CustomResources.HITPOINTS).getFor(ragnaros));
        assertEquals(1, ResourceRetriever.forResource(CustomResources.CANT_ATTACK).getFor(ragnaros));

        game.startGame();

        PhaseController phaseController = Retrievers.singleton(game, PhaseController.class);

        Entity currentPlayer = phaseController.getCurrentEntity();
        BattlefieldComponent battlefieldComponent = currentPlayer.getComponent(BattlefieldComponent.class);
        ragnaros.getComponent(CardComponent.class).moveToBottom(battlefieldComponent);

        phaseController.nextPhase();

        int currentPlayerHealth = CustomResources.HITPOINTS.getFor(phaseController.getCurrentEntity());
        assertEquals(22, currentPlayerHealth);
    }

    public enum CustomResources implements ECSResource {
        ATTACK, HITPOINTS, CANT_ATTACK;
    }

    public enum CustomAttributes implements ECSAttribute {
        NAME;
    }

    public static class CustomModDSL implements ModDSL {
        public static Entity opponent(final ECSGame game) {
            PhaseController phaseController = Retrievers.singleton(game, PhaseController.class);
            return Players.getNextPlayer(phaseController.getCurrentEntity());
        }

        public static List<Entity> characters(final Entity playerEntity) {
            Filters filters = new Filters();
            TargetFilter targetFilter = filters.friendly().and(TargetFilter.or(filters.isPlayer(), filters.isOnBattlefield()));
            return playerEntity.getGame().findEntities(entity -> entity.hasComponent(PlayerComponent.class) || entity.hasComponent(CardComponent.class)).stream()
                .filter(entity -> targetFilter.test(playerEntity, entity))
                .collect(Collectors.toList());
        }

        public static List<Entity> pickRandom(final List<Entity> entities, final int count) {
            Collections.shuffle(entities);
            return entities.subList(0, count);
        }

        public static void dealDamage(final List<Entity> entities, final int damage) {
            ResourceRetriever healthRetriever = ResourceRetriever.forResource(CustomResources.HITPOINTS);
            entities.forEach(entity -> healthRetriever.resFor(entity).change(-damage));
        }
    }

    public static class CustomMod implements ECSMod {
        @Override
        public void declareConfiguration(final ECSGame game) {
            for (int i = 0; i < 2; i++) {
                Entity entity = game.newEntity();
                PlayerComponent playerComponent = new PlayerComponent(i, "Player" + (i + 1));
                entity.addComponent(playerComponent);
            }
        }

        @Override
        public void setupGame(final ECSGame game) {
            PhaseController phaseController = new PhaseController();
            game.newEntity().addComponent(phaseController);

            for (int i = 0; i < 2; i++) {
                final int playerIndex = i;
                Entity player = game.findEntities(e -> e.hasComponent(PlayerComponent.class) && e.getComponent(PlayerComponent.class).getIndex() == playerIndex).get(0);
                Phase playerPhase = new Phase(player, "Main");
                phaseController.addPhase(playerPhase);

                ECSResourceMap.createFor(player)
                    .set(CustomResources.HITPOINTS, 30);

                ZoneComponent battlefield = new BattlefieldComponent(player);
                player.addComponent(battlefield);
            }

            Entity neutral = game.newEntity();
            ZoneComponent zone = new ZoneComponent(neutral, "Cards");
            neutral.addComponent(zone);

            JsEffectsCardLoader jsEffectsCardLoader = new JsEffectsCardLoader();
            try {
                Path cardsPath = Paths.get(getClass().getResource("js-effects-cards.js").toURI());
                Collection<Entity> cards = jsEffectsCardLoader.loadCards(cardsPath, game, this, CustomResources.values(), CustomAttributes.values());
                cards.forEach(zone::addOnBottom);
            } catch (URISyntaxException | CardLoadingException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public Class<? extends ModDSL> dslClass() {
            return CustomModDSL.class;
        }

        @Override
        public Map<String, Class<?>> getEventMapping() {
            Map<String, Class<?>> map = new HashMap<>();
            map.put("PhaseEndEvent", PhaseEndEvent.class);
            return map;
        }
    }
}