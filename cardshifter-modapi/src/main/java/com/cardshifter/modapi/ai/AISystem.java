package com.cardshifter.modapi.ai;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSGameState;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.Retriever;
import com.cardshifter.modapi.events.StartGameEvent;

public class AISystem implements ECSSystem {

	private static final Logger logger = LogManager.getLogger(AISystem.class);
	
	@Retriever
	private ComponentRetriever<AIComponent> ai;
	
	private final ScheduledExecutorService executor;
	
	public AISystem(ScheduledExecutorService executor) {
		this.executor = executor;
	}

	public static void setup(ECSGame game, ScheduledExecutorService executor) {
		AISystem system = new AISystem(executor);
		game.addSystem(system);
		if (game.getGameState() != ECSGameState.NOT_STARTED) {
			logger.warn("Game has already been started when adding AISystem. Performing AI Check directly");
			system.aiPerform(game);
		}
	}
	
	private void aiPerform(ECSGame game) {
		Set<Entity> ais = game.getEntitiesWithComponent(AIComponent.class);
		
		logger.info("AI entities " + ais);
		for (Entity entity : ais) {
			AIComponent aiComp = ai.get(entity);
			if (aiComp.hasWaitingAction()) {
				logger.info(entity + " already has a waiting action");
				continue;
			}
			
			long delay = aiComp.getDelay();
			ECSAction action = aiComp.getAI().getAction(entity);
			if (action != null && !game.isGameOver()) {
				logger.info(entity + " will perform " + action + " in " + delay + " milliseconds");
				Runnable runnable = () -> this.perform(entity, action);
				if (delay <= 0) {
					runnable.run();
				}
				else {
					ScheduledFuture<?> future = executor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
					aiComp.future = future;
				}
				return;
			}
			else {
				logger.info(entity + ": No actions available");
			}
		}
	}

	private void perform(Entity entity, ECSAction action) {
		try {
			logger.info(entity + " performs " + action);
			if (ai.has(entity)) {
				ai.get(entity).future = null;
			}
			boolean performed = action.perform(entity);
			
			if (!performed) {
				logger.error(entity + " AI cannot perform action " + action);
				aiPerform(entity.getGame());
			}
		}
		catch (Throwable ex) {
			// We're on a different thread, so we need information in case anything goes wrong
			logger.error(entity + " Error performing " + action, ex);
		}
	}

	@Override
	public void startGame(ECSGame game) {
		game.getEvents().registerHandlerAfter(this, ActionPerformEvent.class, event -> this.aiPerform(event.getEntity().getGame()));
		game.getEvents().registerHandlerAfter(this, StartGameEvent.class, event -> this.aiPerform(event.getGame()));
	}

	/**
	 * Call all AIs in the game directly. Useful for when a new AI has been initialized while the game is running
	 * @param game Game to call AIs in
	 */
	public static void call(ECSGame game) {
		Set<Entity> ais = game.getEntitiesWithComponent(AIComponent.class);
		ComponentRetriever<AIComponent> ai = game.componentRetreiver(AIComponent.class);
		
		logger.info("AI entities " + ais);
		for (Entity entity : ais) {
			AIComponent aiComp = ai.get(entity);
			if (aiComp.hasWaitingAction()) {
				logger.info(entity + " already has a waiting action");
				continue;
			}
			
			ECSAction action = aiComp.getAI().getAction(entity);
			if (action != null && !game.isGameOver()) {
				logger.info(entity + " performs " + action);
				action.perform(entity);
				return;
			}
			else {
				logger.info(entity + ": No actions available");
			}
		}
	}
	
}
