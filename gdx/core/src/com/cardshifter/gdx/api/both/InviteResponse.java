package com.cardshifter.gdx.api.both;

import com.cardshifter.gdx.api.messages.Message;

public class InviteResponse extends Message {

	private final int inviteId;
	private final boolean accepted;
	
	public InviteResponse() {
		this(0, false);
	}

	public InviteResponse(int inviteId, boolean accepted) {
		super("inviteResponse");
		this.inviteId = inviteId;
		this.accepted = accepted;
	}
	
	public int getInviteId() {
		return inviteId;
	}
	
	public boolean isAccepted() {
		return accepted;
	}

}
