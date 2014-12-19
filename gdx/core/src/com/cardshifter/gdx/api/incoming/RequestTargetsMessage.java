package com.cardshifter.gdx.api.incoming;

import com.cardshifter.gdx.api.messages.Message;

public class RequestTargetsMessage extends Message {

	private int gameId;
	private int id;
	private String action;

    public RequestTargetsMessage() {
        this(0, 0, "");
    }

    public RequestTargetsMessage(int gameId, int id, String action) {
		super("requestTargets");
		this.gameId = gameId;
		this.id = id;
		this.action = action;
	}
	
	public String getAction() {
		return action;
	}
	
	public int getGameId() {
		return gameId;
	}
	
	public int getId() {
		return id;
	}

    public void setAction(String action) {
        this.action = action;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public void setId(int id) {
        this.id = id;
    }
}
