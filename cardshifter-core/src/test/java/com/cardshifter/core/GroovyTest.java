package com.cardshifter.core;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.core.game.ModCollection;
import com.cardshifter.core.modloader.ECSModTest;
import com.cardshifter.core.modloader.GroovyMod;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.ai.CardshifterAI;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.players.Players;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(AllTests.class)
public class GroovyTest {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        ModCollection mods = new ModCollection();
        suite.addTest(new TestCase("Testing mods " + mods.getAvailableMods()) {
            @Override
            protected void runTest() throws Throwable {
                assertNotSame(0, mods.getAvailableMods().size());
            }
        });

        for (String modName : mods.getAvailableMods()) {
            suite.addTest(createSuite(mods, modName));
        }
        return suite;
    }

    private static Test createSuite(ModCollection mods, String modName) {
        TestSuite suite = new TestSuite(modName);
        ECSMod mod = mods.getModFor(modName);
        if (mod instanceof GroovyMod) {
            GroovyMod groovyMod = (GroovyMod) mod;
            List<ECSModTest> tests = groovyMod.testList();
            if (tests == null) {
                suite.addTest(untestedMod(modName));
                return suite;
            }
            tests.add(sequencialPlayTest(mods, modName));
            for (ECSModTest test : tests) {
                suite.addTest(createTest(mods, modName, test));
            }
        }
        return suite;
    }

    private static ECSModTest sequencialPlayTest(ModCollection mods, String modName) {
        ECSModTest modTest = new ECSModTest("quick play", new Runnable() {
            @Override
            public void run() {
                ECSMod mod = mods.getModFor(modName);
                ECSGame game = new ECSGame();
                CardshifterAI ai = new ScoringAI(AIs.fighter());
                mod.declareConfiguration(game);
                List<Entity> players = Players.getPlayersInGame(game);
                for (Entity entity : players) {
                    ai.configure(entity, entity.getComponent(ConfigComponent.class));
                }
                mod.setupGame(game);
                game.startGame();
                while (!game.isGameOver()) {
                    boolean performed = false;
                    for (Entity entity : players) {
                        ECSAction action = ai.getAction(entity);
                        if (action != null) {
                            boolean doSomething = action.perform(entity);
                            if (doSomething) {
                                System.out.println(entity + " performed " + action);
                            }
                            performed = performed || doSomething;
                        }
                    }
                    assertTrue("No player perfored any action: " + players, performed);
                }
            }
        });
        return modTest;
    }

    private static Test untestedMod(String modName) {
        return new TestCase(modName) {
            @Override
            protected void runTest() throws Throwable {
                fail("mod does not contain a 'test.groovy' file: " + modName);
            }
        };
    }

    private static Test createTest(ModCollection mods, String modName, ECSModTest test) {
        return new TestCase(test.getName()) {
            private ECSMod mod;

            @Override
            protected void setUp() throws Exception {
                mod = mods.getModFor(modName);
            }

            @Override
            protected void runTest() throws Throwable {
                test.getRunnable().run();
            }

            @Override
            protected void tearDown() throws Exception {
            }
        };
    }

}
