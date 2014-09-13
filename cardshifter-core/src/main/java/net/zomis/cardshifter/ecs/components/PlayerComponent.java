package net.zomis.cardshifter.ecs.components;

import net.zomis.cardshifter.ecs.base.Component;

public class PlayerComponent extends Component {

	private final String name;

	public PlayerComponent(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
