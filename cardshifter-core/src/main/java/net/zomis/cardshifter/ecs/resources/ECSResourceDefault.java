package net.zomis.cardshifter.ecs.resources;

public class ECSResourceDefault implements ECSResource {
	
	private final String name;

	public ECSResourceDefault(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
