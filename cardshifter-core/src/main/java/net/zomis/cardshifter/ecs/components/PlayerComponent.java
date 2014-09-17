package net.zomis.cardshifter.ecs.components;

import net.zomis.cardshifter.ecs.base.Component;

public class PlayerComponent extends Component {

	private final int index;
	private final String name;

	public PlayerComponent(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public int getIndex() {
		return index;
	}
	
}
