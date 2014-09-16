package net.zomis.cardshifter.ecs.base;

import java.util.Set;

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

	public static <T extends Component> ComponentRetriever<T> retreiverFor(Class<T> clazz) {
		return new ComponentRetriever<>(clazz);
	}

	public static <T extends Component> ComponentRetriever<T> singleton(Class<T> class1) {
		return new ComponentRetriever<T>(null) {
			
			@Override
			public boolean has(Entity entity) {
				return get(entity) != null;
			}
			
			@Override
			public T get(Entity entity) {
				return singleton(entity.getGame(), class1);
			}
			
		};
	}

	public static <T extends Component> T singleton(ECSGame game, Class<T> class1) {
		Set<Entity> all = game.getEntitiesWithComponent(class1);
		if (all.size() != 1) {
			throw new IllegalStateException();
		}
		return all.iterator().next().getComponent(class1);
	}
	
}
