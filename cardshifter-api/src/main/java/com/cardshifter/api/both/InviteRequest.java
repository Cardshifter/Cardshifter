package com.cardshifter.api.both;

import com.cardshifter.api.messages.Message;

public class InviteRequest extends Message {

	private int id;
	private String name;
	private String gameType;

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
