package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import org.junit.Test;

import static org.junit.Assert.*;

public class HearthstoneGameTest {
    @Test
    public void testStartGame() {
        ECSGame game = new ECSGame();
        ECSMod mod = new HearthstoneGame();
        mod.declareConfiguration(game);
        mod.setupGame(game);

        game.startGame();
    }
}