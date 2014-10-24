package net.zomis.cardshifter.ecs;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.phase.PhaseEndEvent;
import com.cardshifter.modapi.players.Players;

public class UntilEndOfOwnerTurnSystem implements ECSSystem {

	private final Entity owner;
	private final ECSSystem system;

	public UntilEndOfOwnerTurnSystem(Entity entity, ECSSystem system) {
		this.owner = Players.findOwnerFor(entity);
		this.system = system;
	}

	@Override
	public void startGame(ECSGame game) {
		game.addSystem(system);
		game.getEvents().registerHandlerAfter(system, PhaseEndEvent.class, this::phaseEnd);
	}
	
	private void phaseEnd(PhaseEndEvent event) {
		if (owner == event.getOldPhase().getOwner()) {
			owner.getGame().removeSystem(system);
		}
	}

}
