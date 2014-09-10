package net.zomis.cardshifter.ecs.base;

public class ComponentRetriever<T extends Component> {

	private final Class<T> clazz;

	public ComponentRetriever(Class<T> clazz) {
		this.clazz = clazz;
	}

	public boolean has(Entity entity) {
		return entity.hasComponent(clazz);
	}

	public T get(Entity entity) {
		return entity.getComponent(clazz);
	}

}
