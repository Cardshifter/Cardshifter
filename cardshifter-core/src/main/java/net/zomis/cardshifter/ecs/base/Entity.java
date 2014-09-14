package net.zomis.cardshifter.ecs.base;

import java.util.HashMap;
import java.util.Map;

public class Entity {

	private Map<Class<? extends Component>, Object> components = new HashMap<>();
	
	private final int id;
	private final ECSGame game;
	
	public Entity(ECSGame game, int id) {
		this.game = game;
		this.id = id;
	}

	public Entity addComponent(Component component) {
		components.put(component.getClass(), component);
		component.setEntity(this);
		return this;
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
	
	public ECSGame getGame() {
		return game;
	}

	public void addComponents(Component... components) {
		for (Component component : components) {
			this.addComponent(component);
		}
	}
	
}
