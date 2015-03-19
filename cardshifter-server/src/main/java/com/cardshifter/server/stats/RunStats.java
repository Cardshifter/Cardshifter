package com.cardshifter.server.stats;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.api.CardshifterConstants;
import com.cardshifter.core.game.ModCollection;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.attack.AttackEvent;
import com.cardshifter.modapi.actions.attack.DamageEvent;
import com.cardshifter.modapi.ai.AIComponent;
import com.cardshifter.modapi.ai.AISystem;
import com.cardshifter.modapi.base.*;
import com.cardshifter.modapi.cards.DrawCardFailedEvent;
import com.cardshifter.modapi.cards.ZoneChangeEvent;
import com.cardshifter.modapi.events.EntityRemoveEvent;
import com.cardshifter.modapi.events.GameOverEvent;
import com.cardshifter.modapi.events.StartGameEvent;
import com.cardshifter.modapi.phase.PhaseChangeEvent;
import com.cardshifter.modapi.phase.PhaseEndEvent;
import com.cardshifter.modapi.phase.PhaseStartEvent;
import com.cardshifter.modapi.players.Players;
import com.cardshifter.modapi.resources.ResourceValueChange;
import com.cardshifter.server.main.ServerMain;
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import net.zomis.fight.statextract.Extractor;
import net.zomis.fight.statextract.IndexableResults;
import net.zomis.fight.statextract.InstancePoster;
import org.apache.log4j.PropertyConfigurator;

import java.util.List;

/**
 * Created by Simon on 3/19/2015.
 */
public class RunStats {

    private static final int GAMES = 1;

    public static void main(String[] args) {
        PropertyConfigurator.configure(ServerMain.class.getResource("log4j.properties"));
        new RunStats().run();
    }

    private final Extractor extractor = Extractor.extractor(new StatsGame());
    private final ModCollection mods = new ModCollection();

    private void run() {
        for (int i = 0; i < GAMES; i++) {
            playGame();
        }
        IndexableResults results = extractor.collectIndexable();
        System.out.println(results.unindexed().getData());
    }

    private void playGame() {
        ECSMod mod = mods.getModFor(CardshifterConstants.VANILLA);
        final InstancePoster instance = extractor.postPrimary();
        ECSGame game = new ECSGame();
        game.setRandomSeed(42);
        game.getEvents().registerHandlerAfter(this, GameOverEvent.class, e -> instance.post(e));
        game.getEvents().registerHandlerAfter(this, StartGameEvent.class, e -> instance.post(e));
        game.getEvents().registerHandlerAfter(this, ActionPerformEvent.class, instance::post);
        game.getEvents().registerHandlerAfter(this, AttackEvent.class, instance::post);
        game.getEvents().registerHandlerAfter(this, DamageEvent.class, instance::post);
        game.getEvents().registerHandlerAfter(this, PlayerEliminatedEvent.class, instance::post);
        game.getEvents().registerHandlerAfter(this, ZoneChangeEvent.class, instance::post);
        game.getEvents().registerHandlerAfter(this, DrawCardFailedEvent.class, instance::post);
        game.getEvents().registerHandlerAfter(this, EntityRemoveEvent.class, instance::post);
        game.getEvents().registerHandlerAfter(this, PhaseStartEvent.class, instance::post);
        game.getEvents().registerHandlerAfter(this, PhaseEndEvent.class, instance::post);
        game.getEvents().registerHandlerAfter(this, ResourceValueChange.class, instance::post);
        game.getEvents().registerHandlerAfter(this, ZoneChangeEvent.class, instance::post);
        mod.declareConfiguration(game);
        List<Entity> players = Players.getPlayersInGame(game);
        players.get(0).addComponent(new AIComponent(new ScoringAI(AIs.medium())));
        players.get(1).addComponent(new AIComponent(new ScoringAI(AIs.fighter())));
        for (Entity entity : players) {
            entity.getComponent(AIComponent.class).getAI().configure(entity, entity.getComponent(ConfigComponent.class));
        }
        mod.setupGame(game);
        game.startGame();
        while (!game.isGameOver()) {
            AISystem.call(game);
        }
    }

}
