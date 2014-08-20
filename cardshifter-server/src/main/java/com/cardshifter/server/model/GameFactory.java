package com.cardshifter.server.model;

public interface GameFactory {
	ServerGame newGame(Server server, int id);
}
