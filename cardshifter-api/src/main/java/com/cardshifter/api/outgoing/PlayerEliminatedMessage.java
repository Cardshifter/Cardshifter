package com.cardshifter.api.outgoing;

import com.cardshifter.api.messages.Message;

/** Informs players that a player has been eliminated, with the status of how that player did. */
public class PlayerEliminatedMessage extends Message {

	private int id;
	private boolean winner;
	private int resultPosition;

	/** Constructor. (no params) */
    public PlayerEliminatedMessage() {
		this(0, false, 0);
	}

	/**
	 * Creates a new message with the specified values
	 *
	 * @param id  This entity
	 * @param winner Whether or not the player is considered a winner
	 * @param resultPosition The result position of the player
	 */
	public PlayerEliminatedMessage(int id, boolean winner, int resultPosition) {
		super("elimination");
		this.id = id;
		this.winner = winner;
		this.resultPosition = resultPosition;
	}

    public int getId() {
        return id;
    }

    public boolean isWinner() {
        return winner;
    }

    public int getResultPosition() {
        return resultPosition;
    }

    @Override
	public String toString() {
		return "PlayerEliminatedMessage ["
				+ "id=" + id 
				+ ", winner=" + winner
				+ ", resultPosition=" + resultPosition
				+ "]";
	}

}
