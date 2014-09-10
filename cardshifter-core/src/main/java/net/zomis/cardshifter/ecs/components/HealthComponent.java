package net.zomis.cardshifter.ecs.components;

import net.zomis.cardshifter.ecs.base.Component;

public class HealthComponent implements Component {

	private int health;

	public HealthComponent(int health) {
		this.health = health;
	}

	public int getHealth() {
		return health;
	}

}
