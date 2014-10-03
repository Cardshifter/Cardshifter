package com.cardshifter.api.both;

import com.cardshifter.api.messages.Message;

public class InviteRequest extends Message {

	private final int id;
	private final String name;

	InviteRequest() {
		this(0, "");
	}
	public InviteRequest(int id, String name) {
		super("inviteRequest");
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

}
