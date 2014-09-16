package net.zomis.cardshifter.ecs.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.zomis.cardshifter.ecs.events.StartGameEvent;

public class ECSGame {

	private final AtomicInteger ids = new AtomicInteger();
	private final Map<Integer, Entity> entities = new HashMap<>();
	private final EventExecutor events = new EventExecutor();
	private final List<ECSSystem> systems = new ArrayList<>();
	private final Random random = new Random();
	private ECSGameState gameState = ECSGameState.NOT_STARTED;
	
	public ECSGame() {
	}
	
	public Entity newEntity() {
		Entity entity = new Entity(this, ids.getAndIncrement());
		this.entities.put(entity.getId(), entity);
		return entity;
	}
	
	/**
	 * Execute a pre-event, perform something, then execute a post-event.
	 * 
	 * @param event Event to execute
	 * @param runInBetween What to do between pre- and post- events.
	 * @return The event that was executed
	 */
	public <T extends IEvent> T executeEvent(T event, Runnable runInBetween) {
		this.events.executePreEvent(event);
		runInBetween.run();
		this.events.executePostEvent(event);
		return event;
	}

	public <T extends Component> ComponentRetriever<T> componentRetreiver(Class<T> class1) {
		return new ComponentRetriever<T>(class1);
	}
	
	public Set<Entity> getEntitiesWithComponent(Class<? extends Component> clazz) {
		return entities.values().stream().filter(e -> e.hasComponent(clazz)).collect(Collectors.toSet());
	}

	public EventExecutor getEvents() {
		return events;
	}

	public void addSystem(ECSSystem system) {
		this.systems.add(system);
	}
	
	public void startGame() {
		if (gameState != ECSGameState.NOT_STARTED) {
			throw new IllegalStateException("Game is already started");
		}
		systems.forEach(sys -> sys.startGame(this));
		events.executePostEvent(new StartGameEvent(this));
		gameState = ECSGameState.RUNNING;
	}

	public Random getRandom() {
		return random;
	}

	public void endGame() {
		gameState = ECSGameState.GAME_ENDED;
	}
	
	public ECSGameState getGameState() {
		return gameState;
	}

	public boolean isGameOver() {
		return gameState == ECSGameState.GAME_ENDED;
	}

	void removeEntity(Entity entity) {
		entities.remove(entity.getId());
	}

	public List<Entity> findEntities(Predicate<Entity> condition) {
		return entities.values().stream().filter(condition).collect(Collectors.toList());
	}
	
	// TODO: Player component, Zone component for a zone
	// TODO: Actions ++ copy actions. List<Target(s)> ("deal 1 damage to up to three targets and then give up to three targets +1/+1 until end of turn"), Set<ActionOptions>. choose one, choose two
	// TODO: Network inform when a component on an entity is changed (DataChangedEvent? Aspect-oriented? onChange method? ResMap?)
	// TODO: Implement the standard Phrancis game
	// TODO: Enchantments
	
}
