package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.phase.PhaseEndEvent;
import com.cardshifter.modapi.players.Players;

/**
 * Adds a supplied system to the game, and removes it at the end of the turn.
 * 
 * @author Simon Forsberg
 */
public class UntilEndOfOwnerTurnSystem implements ECSSystem {

	private final Entity owner;
	private final ECSSystem system;

	public UntilEndOfOwnerTurnSystem(Entity entity, ECSSystem system) {
		this.owner = Players.findOwnerFor(entity);
		this.system = system;
	}

	/**
	 * Adds the new system to the game, registers with PhaseEndEvent.
	 * 
	 * @param game The game to register to
	 */
	@Override
	public void startGame(ECSGame game) {
		game.addSystem(system);
		game.getEvents().registerHandlerAfter(system, PhaseEndEvent.class, this::phaseEnd);
	}
	
	/**
	 * Remove the system from the game.
	 * 
	 * @param event The PhaseEndEvent object
	 */
	private void phaseEnd(PhaseEndEvent event) {
		if (owner == event.getOldPhase().getOwner()) {
			owner.getGame().removeSystem(system);
		}
	}

}
