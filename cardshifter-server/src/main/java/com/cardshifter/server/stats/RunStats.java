package com.cardshifter.server.stats;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.api.CardshifterConstants;
import com.cardshifter.core.game.FakeClient;
import com.cardshifter.core.game.ModCollection;
import com.cardshifter.core.game.TCGGame;
import com.cardshifter.core.replays.ReplayAction;
import com.cardshifter.core.replays.ReplayPlaybackSystem;
import com.cardshifter.core.replays.ReplayRecordSystem;
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
import com.cardshifter.server.commands.ReplayCommand;
import com.cardshifter.server.main.ServerMain;
import com.cardshifter.server.model.GameFactory;
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import net.zomis.cardshifter.ecs.usage.CardshifterIO;
import net.zomis.fight.statextract.Extractor;
import net.zomis.fight.statextract.IndexableResults;
import net.zomis.fight.statextract.InstancePoster;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
//            playGame();
            replayGame("extract-replay.json");
        }
        IndexableResults results = extractor.collectIndexable();
        System.out.println(results.unindexed().getData());
    }

    private void replayGame(String filename) {
        ReplayRecordSystem replay;
        File file = new File(filename);
        try {
            replay = CardshifterIO.mapper().readValue(file, ReplayRecordSystem.class);
        } catch (IOException e1) {
            throw new RuntimeException("Error loading replay: " + e1.getMessage(), e1);
        }

        String actualMod = replay.getModName() != null ? replay.getModName() : null;
        ECSMod mod = mods.getModFor(actualMod);
        final InstancePoster instance = extractor.postPrimary();

        ECSGame game = new ECSGame();
        ReplayPlaybackSystem playback = new ReplayPlaybackSystem(game, replay);
        game.addSystem(playback);
        registerEvents(game, instance);

        mod.declareConfiguration(game);
        playback.setPlayerConfigs(game);
        // is decks configured correctly?
        mod.setupGame(game);

        game.startGame();
//        while (!game.isGameOver()) {
        while (!playback.isReplayFinished()) {
    //        AISystem.call(game);
            ReplayAction step = playback.nextStep();
            System.out.println("next step: " + step);
        }
    }

    private void playGame() {
        ECSMod mod = mods.getModFor(CardshifterConstants.VANILLA);
        final InstancePoster instance = extractor.postPrimary();
        ECSGame game = new ECSGame();
        game.setRandomSeed(42);
        registerEvents(game, instance);
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

    private void registerEvents(ECSGame game, InstancePoster instance) {
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
    }

}
