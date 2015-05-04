package com.cardshifter.gdx.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.cardshifter.gdx.CardshifterGame;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
            GwtApplicationConfiguration config = new GwtApplicationConfiguration(640, 480);
            config.log = null;
            return config;
        }

        @Override
        public ApplicationListener getApplicationListener () {
            return new CardshifterGame(new GWTPlatform());
        }
}