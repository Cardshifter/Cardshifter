package com.cardshifter.core.modloader;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

/**
 * Created by Simon on 3/5/2015.
 */
public class JavaScriptMod implements ECSMod {

    private final ScriptEngine engine;
    private final Exception exception;
    private final Invocable invocable;

    public JavaScriptMod(String name, ScriptEngineManager scripts) {
        engine = scripts.getEngineByName("nashorn");
        this.invocable = (Invocable) engine;
        Exception exception;
        try {
            engine.eval(new InputStreamReader(new FileInputStream(name)));
            exception = null;
        } catch (ScriptException e) {
            exception = e;
        } catch (FileNotFoundException e) {
            exception = e;
        }
        this.exception = exception;
    }

    @Override
    public void declareConfiguration(ECSGame game) {
        if (exception != null) {
            throw new RuntimeException(exception);
        }
        try {
            invocable.invokeFunction("declareConfiguration", game);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setupGame(ECSGame game) {
        if (exception != null) {
            throw new RuntimeException(exception);
        }
        try {
            invocable.invokeFunction("setupGame", game);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
