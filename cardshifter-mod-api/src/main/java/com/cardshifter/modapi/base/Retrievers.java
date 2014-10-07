package com.cardshifter.modapi.base;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

public class Retrievers {

	public static <T extends Component> ComponentRetriever<T> component(Class<T> clazz) {
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
			throw new IllegalStateException("Expected to find exactly one " + class1.getSimpleName() + ", found " + all.size());
		}
		return all.iterator().next().getComponent(class1);
	}

	public static void inject(Object object, ECSGame game) {
		Field[] fields = object.getClass().getDeclaredFields();
		Arrays.stream(fields).filter(field -> field.getAnnotation(Retriever.class) != null).forEach(field -> injectField(object, field, game));
		Arrays.stream(fields).filter(field -> field.getAnnotation(RetrieverSingleton.class) != null).forEach(field -> injectSingleton(object, field, game));
	}

	private static void injectSingleton(Object obj, Field field, ECSGame game) {
		Class<? extends Component> clazz = field.getType().asSubclass(Component.class);
		field.setAccessible(true);
		try {
			field.set(obj, Retrievers.singleton(game, clazz));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static void injectField(Object obj, Field field, ECSGame game) {
		if (field.getType() != ComponentRetriever.class) {
			throw new RuntimeException(field.getType() + " is not a ComponentRetriever");
		}

		Type genericFieldType = field.getGenericType();

		if(genericFieldType instanceof ParameterizedType){
			ParameterizedType aType = (ParameterizedType) genericFieldType;
			Type[] fieldArgTypes = aType.getActualTypeArguments();
			Class<?> fieldArgClass = (Class<?>) fieldArgTypes[0];
			try {
				field.setAccessible(true);
				field.set(obj, Retrievers.component(fieldArgClass.asSubclass(Component.class)));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
