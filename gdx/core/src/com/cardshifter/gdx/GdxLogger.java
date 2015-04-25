package com.cardshifter.gdx;

import com.badlogic.gdx.Gdx;
import com.cardshifter.api.LogInterface;

/**
 * Created by Simon on 4/25/2015.
 */
public class GdxLogger implements LogInterface {
    @Override
    public void info(String s) {
        Gdx.app.log("Info", s);
    }

    @Override
    public void error(String s, Throwable throwable) {
        Gdx.app.log("ERROR", s, throwable);
    }
}
