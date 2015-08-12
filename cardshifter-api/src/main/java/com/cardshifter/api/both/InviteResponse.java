package com.cardshifter.api.both;

import com.cardshifter.api.messages.Message;
/**
 * Response to an InviteRequest message.
 */
public class InviteResponse extends Message {

	private int inviteId;
	private boolean accepted;
	
	/** Constructor. (no params) */
	public InviteResponse() {
		this(0, false);
	}
	/**
	 * Constructor.
	 * @param inviteId  Id of this incoming InviteRequest message
	 * @param accepted  Whether or not the InviteRequest is accepted
	 */
	public InviteResponse(int inviteId, boolean accepted) {
		super("inviteResponse");
		this.inviteId = inviteId;
		this.accepted = accepted;
	}
	/** @return  Id of this incoming InviteRequest message */
	public int getInviteId() {
		return inviteId;
	}
	/** @return  Whether or not the InviteRequest is accepted */
	public boolean isAccepted() {
		return accepted;
	}

}
