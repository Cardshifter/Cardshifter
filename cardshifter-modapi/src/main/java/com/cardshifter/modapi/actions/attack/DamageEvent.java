package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.events.IEvent;

public class DamageEvent implements IEvent {

	private final Entity target;
	private final Entity damagedBy;
    private final IEvent causedBy;
	private int damage;

	public DamageEvent(IEvent causedBy, Entity target, Entity damagedBy, int damage) {
		this.target = target;
		this.damagedBy = damagedBy;
        this.causedBy = causedBy;
		this.damage = damage;
	}
	
	public Entity getDamagedBy() {
		return damagedBy;
	}
	
	public Entity getTarget() {
		return target;
	}

    public IEvent getCausedBy() {
        return causedBy;
    }

    public int getDamage() {
		return damage;
	}
	
	public void setDamage(int damage) {
		this.damage = damage;
	}

}
