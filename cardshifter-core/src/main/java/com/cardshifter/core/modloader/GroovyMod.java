package com.cardshifter.core.modloader;

import com.cardshifter.core.groovy.GroovyRunner;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Created by Simon on 3/5/2015.
 */
public class GroovyMod implements ECSMod {

    private final Throwable exception;
    private GroovyModInterface script;

    public GroovyMod(File directory, String name) {
        Throwable ex = null;

        try {
            URL groovyURL = GroovyMod.class.getResource("");

            GroovyScriptEngine scriptEngine = new GroovyScriptEngine(new URL[]{groovyURL, directory.toURI().toURL()});
            CompilerConfiguration config = new CompilerConfiguration();
            scriptEngine.setConfig(config);

            script = new GroovyRunner(directory, name, scriptEngine.getGroovyClassLoader());
        } catch (Exception | AssertionError e) {
            ex = e;
        }

        this.exception = ex;
    }

    private void ensureInitialized() {
        if (exception != null) {
            throw new RuntimeException("Error initializing mod", exception);
        }
    }

    @Override
    public void declareConfiguration(ECSGame game) {
        ensureInitialized();

        try {
            script.declareConfiguration(game);
        } catch (Exception | AssertionError ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setupGame(ECSGame game) {
        ensureInitialized();

        try {
            script.setupGame(game);
        } catch (Exception | AssertionError ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public List<ECSModTest> testList() {
        ensureInitialized();

        return script.getTests();
    }

}
