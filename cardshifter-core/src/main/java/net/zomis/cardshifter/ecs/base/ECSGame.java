package net.zomis.cardshifter.ecs.base;

import java.util.concurrent.atomic.AtomicInteger;

public class ECSGame {

	private final AtomicInteger ids = new AtomicInteger();
	
	public Entity newEntity() {
		return new Entity(ids.getAndIncrement());
	}

	public <T extends Component> ComponentRetriever<T> componentRetreiver(Class<T> class1) {
		return new ComponentRetriever<T>(class1);
	}

}
