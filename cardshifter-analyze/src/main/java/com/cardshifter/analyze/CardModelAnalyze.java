package com.cardshifter.analyze;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.core.game.ModCollection;
import com.cardshifter.core.game.TCGGame;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.ai.CardshifterAI;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.players.Players;
import net.zomis.cardshifter.ecs.config.ConfigComponent;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CardModelAnalyze {

    public static void main(String[] args) {
        new CardModelAnalyze().run();
    }

    private void run() {
        ModCollection mods = ModCollection.defaultMods();
        mods.loadExternal(mods.getDefaultModLocation());
        ECSMod mod = mods.getModFor("Cyborg-Chronicles");
        ECSGame ecsGame = new ECSGame();
        CardshifterAI ai = mods.getAIs().get("Fighter");

        mod.declareConfiguration(ecsGame);
        for (Entity playerEntity : Players.getPlayersInGame(ecsGame)) {
            ai.configure(playerEntity, playerEntity.getComponent(ConfigComponent.class));
        }
        mod.setupGame(ecsGame);
        ecsGame.startGame();

        while (!ecsGame.isGameOver()) {
            for (Entity player : Players.getPlayersInGame(ecsGame)) {
                ECSAction action = ai.getAction(player);
                if (action != null && action.isAllowed(player)) {
                    System.out.println(player + " Performing " + action);
                    action.perform(player);
                }
            }
        }

    }

}
