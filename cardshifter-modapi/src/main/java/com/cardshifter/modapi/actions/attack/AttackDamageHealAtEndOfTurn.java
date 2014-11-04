package com.cardshifter.modapi.actions.attack;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.phase.PhaseEndEvent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

/**
 * Return the health of all units on the battlefield to full at
 * the end of the turn.
 * 
 * @author Simon Forsberg
 */
public class AttackDamageHealAtEndOfTurn implements ECSSystem {

	private final ResourceRetriever health;
	private final ResourceRetriever maxHealth;

	public AttackDamageHealAtEndOfTurn(ECSResource health, ECSResource maxHealth) {
		this.health = ResourceRetriever.forResource(health);
		this.maxHealth = ResourceRetriever.forResource(maxHealth);
	}
	
	/**
	 * Registers with PhaseEndEvent.
	 * 
	 * @param game The game to register with
	 */
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, PhaseEndEvent.class, this::heal);
	}
	
	/**
	 * Gets all the entities of the owner of the supplied event that are on the battlefield;
	 * If the maxHealth resource has the card entity, its health is set to the maxHealth value.
	 * 
	 * @param event The PhaseEndEvent object
	 */
	private void heal(PhaseEndEvent event) {
		Entity owner = event.getOldPhase().getOwner();
		if (owner == null) {
			return;
		}
		
		Set<Entity> battlefielders = owner.getGame().getEntitiesWithComponent(BattlefieldComponent.class);
		List<Entity> cards = battlefielders.stream().map(entity -> entity.getComponent(BattlefieldComponent.class)).flatMap(comp -> comp.stream()).collect(Collectors.toList());
		for (Entity card : cards) {
			if (maxHealth.has(card)) {
				health.resFor(card).set(maxHealth.getFor(card));
			}
		}
	}

}
