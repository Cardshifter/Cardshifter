package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.base.Component;

public class NameComponent extends Component {

	private final String name;

	public NameComponent(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "Name:" + name;
	}
	
}
