package com.cardshifter.gdx.api.both;

import com.cardshifter.gdx.api.messages.Message;

public class InviteRequest extends Message {

	private final int id;
	private final String name;
	private final String gameType;

	public InviteRequest() {
		this(0, "", "");
	}
	public InviteRequest(int id, String name, String gameType) {
		super("inviteRequest");
		this.id = id;
		this.name = name;
		this.gameType = gameType;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getGameType() {
		return gameType;
	}

}
