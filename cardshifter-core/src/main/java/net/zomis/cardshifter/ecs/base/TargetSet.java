package net.zomis.cardshifter.ecs.base;

import java.util.ArrayList;
import java.util.List;

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
