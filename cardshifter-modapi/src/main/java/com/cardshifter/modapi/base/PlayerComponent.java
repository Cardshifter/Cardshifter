package com.cardshifter.modapi.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerComponent extends Component {

	private final int index;
	private int resultPosition;
	private Boolean winnerDeclaration;
	private String name;

	public PlayerComponent(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	/**
	 * Declare this player as having lost the game 
	 */
	public void loseGame() {
		this.eliminate(false);
	}
	
	/**
	 * Declare this player as having won the game
	 */
	public void winGame() {
		this.eliminate(true);
	}
	
	private void eliminate(boolean winner) {
		List<Entity> players = new ArrayList<>(getEntity().getGame().getEntitiesWithComponent(PlayerComponent.class));
		players.sort(Comparator.comparing(e -> e.getComponent(PlayerComponent.class).getIndex()));
		
		// if no one else has been eliminated, the player is at 1st place. Because the player itself has not been eliminated, it should get increased below.
		int playerResultPosition = winner ? 0 : players.size() + 1;
		
		boolean posTaken = false;
		do {
			posTaken = false;
			playerResultPosition += winner ? -1 : +1;
			for (Entity pp : players) {
				PlayerComponent playerComponent = pp.getComponent(PlayerComponent.class);
				if (playerComponent.isEliminated() && playerComponent.getResultPosition() == playerResultPosition) {
					posTaken = true;
					break;
				}
			}
		}
		while (posTaken);
		
		this.eliminate(winner, playerResultPosition);
	}
	
	private void eliminate(boolean winner, int resultPosition) {
		if (this.isEliminated()) {
			// Can't be eliminated more than once.
			return;
		}
		executeCancellableEvent(new PlayerEliminatedEvent(getEntity(), winner, resultPosition), () -> {
			this.resultPosition = resultPosition;
			this.winnerDeclaration = winner;
		});
	}

	/**
	 * @return Return the ranking the player got in this game. 1 is the top winner.
	 */
	public int getResultPosition() {
		return resultPosition;
	}
	
	/**
	 * @return True if this player has been declared as winning or losing the game
	 */
	public boolean isEliminated() {
		return this.resultPosition != 0;
	}
	
	/**
	 * @return True if player was declared winner, false if player was declared loser. Null if player hasn't been eliminated yet.
	 */
	public Boolean getWinnerDeclaration() {
		return winnerDeclaration;
	}

	@Override
	public String toString() {
		return "PlayerComponent [index=" + index + ", name=" + name + "]";
	}

}
