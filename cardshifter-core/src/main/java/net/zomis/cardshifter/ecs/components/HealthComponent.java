package net.zomis.cardshifter.ecs.components;

import net.zomis.cardshifter.ecs.base.Component;

@Deprecated
public class HealthComponent extends Component {

	private int health;
//	private IntegerValue healthValue = new IntegerValue(this);

	public HealthComponent(int health) {
		this.health = health;
	}

	public int getHealth() {
		return health;
	}

	@Deprecated
	public void damage(int damage) {
//		executeEvent(new DamageEvent(damage), () -> {
//			this.health -= damage;
//		});
	}

}
