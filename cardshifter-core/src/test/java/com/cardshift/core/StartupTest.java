package com.cardshift.core;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class StartupTest {
	@Test
	public void createGameWithDecks() {
		assertTrue(true);
		File file = new File(Game.class.getResource("start.lua").getPath()).getParentFile();
		Game game = new Game(file);
		game.getEvents().startGame(game);
		
		assertEquals(42, game.getPlayers().get(0).data.get("life").toint());
	}
}
