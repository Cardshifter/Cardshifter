package com.cardshifter.core;

import com.cardshifter.core.game.ModCollection;
import com.cardshifter.core.modloader.ECSModTest;
import com.cardshifter.core.modloader.GroovyMod;
import com.cardshifter.modapi.base.ECSMod;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import java.util.List;

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
            for (ECSModTest test : tests) {
                suite.addTest(createTest(mods, modName, test));
            }
        }
        return suite;
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
