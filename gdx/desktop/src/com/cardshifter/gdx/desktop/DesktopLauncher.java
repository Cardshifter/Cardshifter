package com.cardshifter.gdx.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.NonGWTPlatform;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1024;
		config.height = 600;
		new LwjglApplication(new CardshifterGame(new NonGWTPlatform()), config);
	}
}
