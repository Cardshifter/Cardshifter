package com.cardshifter.core;

import static org.junit.Assert.*;

import org.junit.Test;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

public class StartupTest {
	@Test
	public void createGameWithDecks() {
		Game game = new Game(Game.class.getResourceAsStream("start.lua"));
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
		UsableAction action = card.getAction("Use");
		assertTrue(action.isAllowed());
		action.perform();
		assertEquals(41, game.getPlayers().get(1).data.get("life").toint());
		assertFalse(card.hasZone());
	}
}
