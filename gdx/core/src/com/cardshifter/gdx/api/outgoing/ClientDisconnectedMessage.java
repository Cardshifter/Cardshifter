package com.cardshifter.gdx.api.outgoing;

import com.cardshifter.gdx.api.messages.Message;

public class ClientDisconnectedMessage extends Message {

	private String name;
	private int playerIndex;

	public ClientDisconnectedMessage() {
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

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayerIndex(int playerIndex) {
        this.playerIndex = playerIndex;
    }
}
