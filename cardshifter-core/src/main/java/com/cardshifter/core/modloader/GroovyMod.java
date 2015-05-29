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

    private final Throwable exception;
    private ECSMod script;

    public GroovyMod(String name) {
        File file = new File("groovy/" + name);
        Throwable ex;
        try {
            Binding binding = new Binding();
            URL groovyURL = getClass().getClassLoader().getResource("groovy/");
            GroovyScriptEngine scriptEngine = new GroovyScriptEngine(new URL[]{file.toURI().toURL(), groovyURL});

            CompilerConfiguration config = new CompilerConfiguration();
            scriptEngine.setConfig(config);
            binding.setVariable("script", name);
            binding.setVariable("cl", scriptEngine.getGroovyClassLoader());

            script = (ECSMod) scriptEngine.run("GroovyRunner.groovy", binding);
            ex = null;
        } catch (Exception | AssertionError e) { // MalformedURLException | ResourceException | groovy.util.ScriptException |
            ex = e;
        }
        this.exception = ex;
    }

    @Override
    public void declareConfiguration(ECSGame game) {
        if (exception != null) {
            throw new RuntimeException("Error initializing mod", exception);
        }
        try {
            script.declareConfiguration(game);
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
