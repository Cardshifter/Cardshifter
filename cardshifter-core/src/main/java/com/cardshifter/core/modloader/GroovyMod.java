package com.cardshifter.core.modloader;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.net.URL;

/**
 * Created by Simon on 3/5/2015.
 */
public class GroovyMod implements ECSMod {

    private final GroovyScriptEngine engine;
    private final Binding binding = new Binding();
    private final Exception exception;
    private ECSMod script;

    public GroovyMod(String name) {
        File file = new File("groovy/" + name);
        Exception ex;
        GroovyScriptEngine scriptEngine;
        try {
            URL groovyURL = getClass().getClassLoader().getResource("groovy/");
            scriptEngine = new GroovyScriptEngine(new URL[]{file.toURI().toURL(), groovyURL});
            CompilerConfiguration config = new CompilerConfiguration();
            scriptEngine.setConfig(config);
            this.script = (ECSMod) scriptEngine.run("Game.groovy", binding);
            ex = null;
        } catch (Exception e) { // MalformedURLException | ResourceException | groovy.util.ScriptException |
            scriptEngine = null;
            ex = e;
        }
        this.exception = ex;
        this.engine = scriptEngine;
    }

    @Override
    public void declareConfiguration(ECSGame game) {
        if (exception != null) {
            throw new RuntimeException("Error initializing mod", exception);
        }
        try {
            script.declareConfiguration(game);
            System.out.println("declare config complete");
        } catch (Exception | AssertionError ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setupGame(ECSGame game) {
        if (exception != null) {
            throw new RuntimeException("Error initializing mod", exception);
        }
        try {
            script.setupGame(game);
        } catch (Exception | AssertionError ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
