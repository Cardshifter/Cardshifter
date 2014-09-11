package net.zomis.cardshifter.ecs.base;

public class Phase {

	private final Entity owner;
	private final String name;

	public Phase(Entity owner, String name) {
		this.owner = owner;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Entity getOwner() {
		return owner;
	}
	
}
