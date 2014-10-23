package com.cardshifter.server.model;

import com.cardshifter.core.game.ServerGame;

public interface GameFactory {
	ServerGame newGame(Server server, int id);
}
