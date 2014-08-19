package com.cardshift.core;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

public class StartupTest {
	@Test
	public void createGameWithDecks() {
		assertTrue(true);
		File file = new File(Game.class.getResource("start.lua").getPath()).getParentFile();
		Game game = new Game(file);
		game.getEvents().startGame(game);
		
		assertEquals(2, game.getPlayers().size());
		
		assertEquals(42, game.getPlayers().get(0).data.get("life").toint());
		assertEquals(42, game.getPlayers().get(1).data.get("life").toint());
		
		assertEquals(2, game.getZones().size());
		
		for (Zone zone : game.getZones()) {
			assertEquals(3, zone.getCards().size());
		}
		
		LuaValue value = game.getPlayers().get(0).data.get("battlefield");
		Zone zone = (Zone) CoerceLuaToJava.coerce(value, Zone.class);
		assertNotNull(zone);
		
		Card card = zone.getTopCard();
		Action action = card.getAction("Use");
		action.isAllowed(card);
		action.perform(card);
		assertEquals(41, game.getPlayers().get(1).data.get("life").toint());
		assertNull(card.getZone());
	}
}
