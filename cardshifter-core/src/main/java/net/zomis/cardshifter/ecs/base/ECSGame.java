package net.zomis.cardshifter.ecs.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ECSGame {

	private final AtomicInteger ids = new AtomicInteger();
	private final Map<Integer, Entity> entities = new HashMap<>();
	
	public ECSGame() {
	}
	
	public Entity newEntity() {
		Entity entity = new Entity(this, ids.getAndIncrement());
		this.entities.put(entity.getId(), entity);
		return entity;
	}

	public <T extends Component> ComponentRetriever<T> componentRetreiver(Class<T> class1) {
		return new ComponentRetriever<T>(class1);
	}
	
	public Set<Entity> getEntitiesWithComponent(Class<? extends Component> clazz) {
		return entities.values().stream().filter(e -> e.hasComponent(clazz)).collect(Collectors.toSet());
	}

	// TODO: Looping linked list for phases
	// TODO: Player component, Zone component for a zone, MyZoneSetupComponent? Hand+Deck+Battlefield-Component
	// TODO: Actions ++ copy actions. List<Target(s)> ("deal 1 damage to up to three targets and then give up to three targets +1/+1 until end of turn"), Set<ActionOptions>. choose one, choose two
	// TODO: Network inform when a component on an entity is changed (DataChangedEvent? Aspect-oriented? onChange method? ResMap?)
	// TODO: Define the standard Phrancis cards
	
	
}
