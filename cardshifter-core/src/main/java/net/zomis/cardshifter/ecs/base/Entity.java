package net.zomis.cardshifter.ecs.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Entity {

	private Map<Class<? extends Component>, Component> components = new HashMap<>();
	
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
	
	public <T extends Component> T getComponent(Class<T> clazz) {
		return clazz.cast(components.get(clazz));
	}
	
	public <T extends Component> T get(ComponentRetriever<T> retriever) {
		return retriever.get(this);
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

	/**
	 * Get all components that are subtypes of a specific class
	 * 
	 * @param compoentClass
	 * @return
	 */
	public <T extends Component> Collection<T> getSuperComponents(Class<T> compoentClass) {
		return this.components.entrySet().stream()
				.filter(entry -> compoentClass.isAssignableFrom(entry.getKey()))
				.map(entry -> compoentClass.cast(entry.getValue()))
				.collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return "Entity #" + id;
	}

	public void destroy() {
		components.clear();
		game.removeEntity(this);
	}
	
}
