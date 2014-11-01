package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceDefault;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class TrampleSystem implements ECSSystem {

	public static final ECSResource trample = new ECSResourceDefault("TRAMPLE");
	public static final ResourceRetriever trampleRes = ResourceRetriever.forResource(trample);
	private final ResourceRetriever health;
	
	public TrampleSystem(ECSResource health) {
		this.health = ResourceRetriever.forResource(health);
	}
	
	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, DamageEvent.class, this::attack);
	}
	
	private void attack(DamageEvent event) {
		if (trampleRes.getOrDefault(event.getDamagedBy(), 0) <= 0) {
			return;
		}
		
		int overload = event.getDamage() - health.getFor(event.getTarget());
		
		if (overload > 0) {
			Entity owner = Cards.getOwner(event.getTarget());
			health.resFor(owner).change(-overload);
		}
		
	}
	
	
	
}
