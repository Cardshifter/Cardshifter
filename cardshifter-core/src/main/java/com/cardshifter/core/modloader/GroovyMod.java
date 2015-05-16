package com.cardshifter.core.modloader;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.net.MalformedURLException;
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
            scriptEngine = new GroovyScriptEngine(new URL[]{file.toURI().toURL()});
            CompilerConfiguration config = new CompilerConfiguration();
            scriptEngine.setConfig(config);
            this.script = (ECSMod) scriptEngine.run("Game.groovy", binding);
            ex = null;
        } catch (MalformedURLException | ResourceException | groovy.util.ScriptException e) {
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
        script.declareConfiguration(game);
    }

    @Override
    public void setupGame(ECSGame game) {
        if (exception != null) {
            throw new RuntimeException("Error initializing mod", exception);
        }
        script.setupGame(game);
    }
}
