package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

public class ClientDisconnectedMessage extends Message {

	private final String name;
	private final int playerIndex;

	ClientDisconnectedMessage() {
		this("", 0);
	}

	public ClientDisconnectedMessage(String name, int playerIndex) {
		super("disconnect");
		this.name = name;
		this.playerIndex = playerIndex;
	}
	
	public String getName() {
		return name;
	}
	
	public int getPlayerIndex() {
		return playerIndex;
	}

}
