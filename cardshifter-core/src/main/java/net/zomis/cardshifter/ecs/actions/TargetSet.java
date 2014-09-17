package net.zomis.cardshifter.ecs.actions;

import java.util.ArrayList;
import java.util.List;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.EventExecutor;

public class TargetSet {

	private final List<Entity> chosenTargets;
	private final int min;
	private final int max;
	private final ECSAction action;
	
	public TargetSet(ECSAction action, int min, int max) {
		this.chosenTargets = new ArrayList<>(max);
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

	public List<Entity> getTargets() {
		return new ArrayList<>(chosenTargets);
	}

	public void clearTargets() {
		chosenTargets.clear();
	}
	
}
