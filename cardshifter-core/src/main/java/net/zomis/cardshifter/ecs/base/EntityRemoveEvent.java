package net.zomis.cardshifter.ecs.base;

public class EntityRemoveEvent implements IEvent {

	private final Entity entity;

	public EntityRemoveEvent(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}

}
