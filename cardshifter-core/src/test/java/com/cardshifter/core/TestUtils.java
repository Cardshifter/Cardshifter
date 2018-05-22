package com.cardshifter.core;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.core.game.ModCollection;
import com.cardshifter.core.modloader.ECSModTest;
import com.cardshifter.core.modloader.GroovyMod;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.ai.CardshifterAI;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSGameState;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.players.Players;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Simon on 6/3/2015.
 */
public class TestUtils {

    ModCollection createModCollection() {
        ModCollection mods = new ModCollection();
        mods.loadExternal(new File("../test-resources/mods").toPath());
        return mods.loadDefault();
    }

    TestSuite testSuite() {
        PropertyConfigurator.configure(getClass().getClassLoader().getResource("log4j.properties"));
        TestSuite suite = new TestSuite();
        ModCollection mods = createModCollection();

        System.out.println("Mods found " + mods.getAvailableMods().size());
        suite.addTest(new TestCase("Testing mods " + mods.getAvailableMods()) {
            @Override
            protected void runTest() throws Throwable {
                assertNotSame(0, mods.getAvailableMods().size());
                assertNotNull("TestMod not found", mods.getModFor("TestMod"));
            }
        });

        for (String modName : mods.getAvailableMods()) {
            System.out.println("Adding tests for " + modName);
            suite.addTest(createSuite(mods, modName));
        }
        return suite;

    }

    private Test createSuite(ModCollection mods, String modName) {
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
            tests.add(multithreadedPlayTest(mods, modName));
            for (ECSModTest test : tests) {
                suite.addTest(createTest(mods, modName, test));
            }
        }
        return suite;
    }

    private ECSModTest sequencialPlayTest(ModCollection mods, String modName) {
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
                    assertTrue("No player performed an action: " + players, performed);
                }
            }
        });
        return modTest;
    }

    private ECSModTest multithreadedPlayTest(ModCollection mods, String modName) {
        ECSModTest modTest = new ECSModTest("multithreaded test", new Runnable() {
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

                List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
                List<Thread> threads = new ArrayList<>();
                for (Entity player : players) {
                    Thread thread = new Thread(() -> {
                        try {
                            while (!game.isGameOver()) {
                                ECSAction action = ai.getAction(player);
                                if (action != null && action.perform(player)) {
                                    System.out.println(player + " performed " + action);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            errors.add(ex);
                        }
                    }, "AI Thread for " + modName + " " + player);
                    threads.add(thread);
                }

                for (Thread th : threads) {
                    th.start();
                }

                for (Thread th : threads) {
                    try {
                        th.join();
                    } catch (InterruptedException e) {
                        fail("Thread interrupted");
                    }
                }
                assertTrue("Errors occurred: " + errors, errors.isEmpty());
                assertTrue(game.isGameOver());
            }
        });
        return modTest;
    }

    private Test untestedMod(String modName) {
        return new TestCase(modName) {
            @Override
            protected void runTest() throws Throwable {
                fail("mod does not contain a 'test.groovy' file: " + modName);
            }
        };
    }

    private Test createTest(ModCollection mods, String modName, ECSModTest test) {
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

    public TestSuite testCreateSuite() {
        System.out.println(new File("").getAbsolutePath());
        TestSuite suite = new TestSuite();
        ModCollection mods = createModCollection();

        System.out.println("Mods found " + mods.getAvailableMods().size());
        suite.addTest(new TestCase("Mods available " + mods.getAvailableMods()) {
            @Override
            protected void runTest() throws Throwable {
                assertNotSame(0, mods.getAvailableMods().size());
            }
        });

        for (String modName : mods.getAvailableMods()) {
            System.out.println("Adding tests for " + modName);
            suite.addTest(new TestCase(modName) {
                @Override
                protected void runTest() throws Throwable {
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
                    assertEquals(ECSGameState.RUNNING, game.getGameState());
                    for (int i = 0; i < 5; i++) {
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
                        assertTrue("No player performed an action: " + players, performed);
                    }
                }
            });
        }
        return suite;
    }
}
