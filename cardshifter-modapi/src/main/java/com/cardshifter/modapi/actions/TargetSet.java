package com.cardshifter.modapi.actions;

import java.util.ArrayList;
import java.util.List;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.events.EventExecutor;

public class TargetSet {

	private final List<Entity> chosenTargets;
	private final int min;
	private final int max;
	private final ECSAction action;
	
	public TargetSet(ECSAction action, int min, int max) {
		this.chosenTargets = new ArrayList<>(min);
		this.action = action;
		this.min = min;
		this.max = max;
	}
	
	public boolean addTarget(Entity target) {
//		events().executePostEvent(new TargetEvent(action, this, target));
		if (!isTargetable(target)) {
			return false;
		}
		chosenTargets.add(target);
		return true;
	}

	private EventExecutor events() {
		return game().getEvents();
	}
	
	private ECSGame game() {
		return action.getOwner().getGame();
	}
	
	public boolean isTargetable(Entity target) {
		TargetableCheckEvent event = new TargetableCheckEvent(action, this, target);
		events().executePostEvent(event);
		return event.isAllowed();
	}
	
	public List<Entity> findPossibleTargets() {
		return game().findEntities(entity -> isTargetable(entity));
	}
	
	public int selectedTargets() {
		return chosenTargets.size();
	}

	public boolean hasEnoughTargets() {
		int targets = chosenTargets.size();
		return targets >= min && targets <= max;
	}

	public List<Entity> getChosenTargets() {
		return new ArrayList<>(chosenTargets);
	}

	public void clearTargets() {
		chosenTargets.clear();
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
}
