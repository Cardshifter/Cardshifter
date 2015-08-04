package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.events.IEvent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceDefault;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class TrampleSystem implements ECSSystem {

	private final ResourceRetriever trampleRes;
	private final ResourceRetriever health;
	
	public TrampleSystem(ECSResource trample, ECSResource health) {
		this.health = ResourceRetriever.forResource(health);
        this.trampleRes = ResourceRetriever.forResource(trample);
	}
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, DamageEvent.class, this::attack);
	}
	
	private void attack(DamageEvent event) {
		if (trampleRes.getOrDefault(event.getDamagedBy(), 0) <= 0) {
			return;
		}
        IEvent cause = event.getCausedBy();
        if (!(cause instanceof AttackEvent)) {
            return;
        }
        AttackEvent attack = (AttackEvent) cause;
        if (attack.getTarget() != event.getTarget()) {
            // Trample does not apply when you are defending
            return;
        }

        // this is performed after damage has been dealt, so check if health is less than 0
		int overload = - health.getFor(event.getTarget());
		
		if (overload > 0) {
			Entity owner = Cards.getOwner(event.getTarget());
			health.resFor(owner).change(-overload);
		}
		
	}
	
	
	
}
