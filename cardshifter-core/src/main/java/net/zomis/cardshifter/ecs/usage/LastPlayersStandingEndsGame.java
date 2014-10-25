package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.base.PlayerEliminatedEvent;

/**
 * Adds functionality that ends the game when there's only one (or less) players remaining in the game
 * 
 * @see PlayerEliminatedEvent
 * @author Simon Forsberg
 */
public class LastPlayersStandingEndsGame implements ECSSystem {

	private final int playersRemainingToEnd;

	/**
	 * Create system that ends game when one (1) player remains
	 */
	public LastPlayersStandingEndsGame() {
		this(1);
	}
	
	/**
	 * Create system that ends game when a specific amount of players remains
	 * 
	 * @param playersRemainingToEnd Number of remaining players for the game to be ended
	 */
	public LastPlayersStandingEndsGame(int playersRemainingToEnd) {
		this.playersRemainingToEnd = playersRemainingToEnd;
	}
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, PlayerEliminatedEvent.class, this::onPlayerEliminated);
	}
	
	private void onPlayerEliminated(PlayerEliminatedEvent event) {
		long numAlive = event.getEntity().getGame()
			.getEntitiesWithComponent(PlayerComponent.class).stream()
			.map(e -> e.getComponent(PlayerComponent.class))
			.filter(pl -> !pl.isEliminated())
			.count();
		if (numAlive <= playersRemainingToEnd) {
			event.getEntity().getGame().endGame();
		}
	}

}
