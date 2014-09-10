package net.zomis.cardshifter.ecs.base;

import java.util.HashMap;
import java.util.Map;

public class Entity {

	private Map<Class<? extends Component>, Object> components = new HashMap<>();
	
	private final int id;
	
	public Entity(int id) {
		this.id = id;
	}

	public void addComponent(Component component) {
		components.put(component.getClass(), component);
	}
	
	public boolean hasComponent(Class<? extends Component> clazz) {
		return components.containsKey(clazz);
	}
	
	public <T> T getComponent(Class<T> clazz) {
		return clazz.cast(components.get(clazz));
	}
	
	public int getId() {
		return id;
	}
	
}
