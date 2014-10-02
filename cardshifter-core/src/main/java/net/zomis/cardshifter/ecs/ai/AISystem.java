package net.zomis.cardshifter.ecs.ai;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSGameState;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.Retriever;
import net.zomis.cardshifter.ecs.events.StartGameEvent;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
			ECSAction action = aiComp.getAI().getAction(entity);
			if (action != null) {
				logger.info(entity + " will perform in 4 seconds " + action);
				executor.schedule(() -> this.perform(entity, action), 4, TimeUnit.SECONDS);
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
	
}
