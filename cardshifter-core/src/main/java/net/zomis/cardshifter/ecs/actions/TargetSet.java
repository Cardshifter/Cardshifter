package net.zomis.cardshifter.ecs.actions;

import java.util.ArrayList;
import java.util.List;

import net.zomis.cardshifter.ecs.base.Entity;

public class TargetSet {

	private final List<Entity> chosenTargets;
	
	public TargetSet(int min, int max) {
		this.chosenTargets = new ArrayList<>(max);
	}
	
	public void addTarget(Entity target) {
		chosenTargets.add(target);
	}

	public List<Entity> findTargets() {
		return new ArrayList<>();
	}

}
