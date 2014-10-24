package com.cardshifter.modapi.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.modapi.events.EventExecutor;
import com.cardshifter.modapi.events.GameOverEvent;
import com.cardshifter.modapi.events.IEvent;
import com.cardshifter.modapi.events.StartGameEvent;

/**
 * Starting point for the entire ECS
 * 
 * @author Simon Forsberg
 */
public final class ECSGame {
	private static final Logger logger = LogManager.getLogger(ECSGame.class);

	private final AtomicInteger ids = new AtomicInteger();
	/**
	 * All the entities of a single game
	 */
	private final Map<Integer, Entity> entities = new HashMap<>();
	private final EventExecutor events = new EventExecutor();
	/**
	 * All the systems that comprise the game
	 */
	private final List<ECSSystem> systems = new ArrayList<>();
	private final Random random = new Random();
	/**
	 * An enum for the current state of the game
	 */
	private ECSGameState gameState = ECSGameState.NOT_STARTED;
	
	public ECSGame() {
	}
	
	/**
	 * Creates an entity, assigns an Id, adds it to the entities of the game object
	 * @return The created entity
	 */
	public Entity newEntity() {
		Entity entity = new Entity(this, ids.incrementAndGet());
		this.entities.put(entity.getId(), entity);
		return entity;
	}
	
	/**
	 * Executes an event while performing something in the middle of executing the event.
	 * It will first do an event for listeners that have registered before, then
	 * it will do runInBetween, then it will fire off the listeners that have registered
	 * for after.
	 * 
	 * @param <T> The event type that is executed
	 * @param event The event to execute
	 * @param runInBetween The action to run in between 
	 * @return The same event that was executed
	 */
	public <T extends IEvent> T executeEvent(T event, Runnable runInBetween) {
		return events.executeEvent(event, runInBetween);
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
		logger.info("Add system: " + system);
		this.systems.add(system);
		Retrievers.inject(system, this);
		if (gameState != ECSGameState.NOT_STARTED) {
			system.startGame(this);
		}
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
		this.executeCancellableEvent(new GameOverEvent(this), () -> gameState = ECSGameState.GAME_ENDED);
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

	public <T extends CancellableEvent> T executeCancellableEvent(T event, Runnable runInBetween) {
		return events.executeCancellableEvent(event, runInBetween);
	}

	public Entity getEntity(int entity) {
		return entities.get(entity);
	}
	
	public void setRandomSeed(long seed) {
		random.setSeed(seed);
	}

	public <T extends ECSSystem> List<T> findSystemsOfClass(Class<T> clazz) {
		return systems.stream()
				.filter(sys -> clazz.isAssignableFrom(sys.getClass()))
				.map(obj -> clazz.cast(obj))
				.collect(Collectors.toList());
	}

	public boolean removeSystem(ECSSystem system) {
		events.removeListenersWithIdentifier(system);
		return systems.remove(system);
	}
	
	// TODO: copy actions. Set<ActionOptions>. choose one, choose two
	// TODO: More Hearthstone-like features. Enchantments, effects, battlecry, deathrattle, etc.
	
}
