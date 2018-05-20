package com.cardshifter.analyze;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.core.game.ModCollection;
import com.cardshifter.core.game.TCGGame;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.ai.CardshifterAI;
import com.cardshifter.modapi.attributes.Attributes;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.players.Players;
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import net.zomis.fight.ext.FightCollectors;
import net.zomis.fight.ext.WinResult;
import net.zomis.fight.v2.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CardModelAnalyze {

    private ModCollection mods = ModCollection.defaultMods();
    private final CardshifterAI ai;

    public CardModelAnalyze() {
        mods.loadExternal(mods.getDefaultModLocation());
        ai = mods.getAIs().get("Fighter");
    }

    public static void main(String[] args) {
        new CardModelAnalyze().fight();
    }

    private void fight() {
        Collector<String, ?, Map<String, Long>> counting = Collectors.groupingBy(e -> e, Collectors.counting());
        StatsExtract<ECSGame> fight = new StatsExtract<ECSGame>()
            .indexes("playerIndex")
            .value("winner", WinResult.class, FightCollectors.stats())
            .value("actionPerformed", String.class, counting)
            .value("cardsPlayed", String.class, counting)
            .dataTuple("playerPerformsAction", Entity.class, ECSAction.class, (stats, player, action) -> {
                int playerIndex = player.getComponent(PlayerComponent.class).getIndex();
                stats.save("actionPerformed", playerIndex, action.getName());
                if (action.getName().equals("Play")) {
                    stats.save("cardsPlayed", playerIndex, action.getOwner().getComponent(ECSAttributeMap.class)
                        .get(Attributes.NAME).get().get());
                }
            })
            .data("gameOver", ECSGame.class, (stats, game, obj) -> {
                List<Entity> players = Players.getPlayersInGame(game);

                for (int i = 0; i < players.size(); i++) {
                    Entity player = players.get(i);
                    boolean won = player.getComponent(PlayerComponent.class).getResultPosition() == 1;
                    stats.save("winner", i, won ? WinResult.WIN : WinResult.LOSS);
                }
                stats.<Integer>index("playerIndex", p -> p);
            });

        Stream<ECSGame> games = Stream.generate(this::createGame).limit(10);

        StatsPerform<ECSGame> perform = this::perform;

        long start = System.nanoTime();
        IndexResults results = StatsFight.performAll(games, perform, fight);
        long end = System.nanoTime();
        System.out.println(TimeUnit.NANOSECONDS.toMillis(end - start) + " ms");
        System.out.println(results.toMultiline());
    }

    private void perform(StatsInterface statsInterface, ECSGame ecsGame, int number) {
        System.out.println("PERFORM NUMBER " + number);
        while (!ecsGame.isGameOver()) {
            for (Entity player : Players.getPlayersInGame(ecsGame)) {
                ECSAction action = ai.getAction(player);
                if (action != null && action.isAllowed(player)) {
                    statsInterface.postTuple("playerPerformsAction", player, action);
                    action.perform(player);
                }
            }
        }
        statsInterface.post("gameOver", ecsGame);
    }

    private ECSGame createGame() {
        ECSMod mod = mods.getModFor("Cyborg-Chronicles");
        ECSGame ecsGame = new ECSGame();
        mod.declareConfiguration(ecsGame);

        for (Entity playerEntity : Players.getPlayersInGame(ecsGame)) {
            ai.configure(playerEntity, playerEntity.getComponent(ConfigComponent.class));
        }
        mod.setupGame(ecsGame);
        ecsGame.startGame();
        return ecsGame;
    }

}
