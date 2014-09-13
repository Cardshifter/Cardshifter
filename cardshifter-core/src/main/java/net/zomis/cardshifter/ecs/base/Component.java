package net.zomis.cardshifter.ecs.base;


public abstract class Component {

	private Entity entity;

	void setEntity(Entity entity) {
		if (this.entity != null) {
			throw new IllegalStateException("Component " + this + " is already connected to another entity.");
		}
		this.entity = entity;
		// TODO: Try to avoid having a Component know about it's entity, but it most importantly need the EventExecutor
	}
	
	protected final void executeEvent(IEvent event) {
		entity.getGame().getEvents().executePostEvent(event);
	}
	
	protected final void executeEvent(IEvent event, Runnable runInBetween) {
		entity.getGame().executeEvent(event, runInBetween);
	}
	
}
