package com.cardshifter.server.model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Set;

import net.zomis.cardshifter.ecs.config.ConfigComponent;

import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.core.game.ServerGame;
import com.cardshifter.core.replays.ReplayAction;
import com.cardshifter.core.replays.ReplayRecordSystem;
import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.Actions;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.actions.TargetSet;
import com.cardshifter.modapi.ai.AIComponent;
import com.cardshifter.modapi.ai.AISystem;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.events.StartGameEvent;

public class ReplayPlaybackSystem implements ECSSystem {

	private static final String NEXT_STEP = "Next Step";

	private final ReplayRecordSystem replayData;
	private int currentActionIndex;

	public ReplayPlaybackSystem(ServerGame game, ReplayRecordSystem replay) {
		this.replayData = replay;
		applySeed(game, replay.getSeed());
	}

	private void applySeed(ServerGame game, long newSeed) {
		Random random = game.getGameModel().getRandom();
		try {
			Field field = random.getClass().getDeclaredField("seed");
			field.setAccessible(true);
			Object result = field.get(random);
			AtomicLong seed = (AtomicLong) result;
			seed.set(newSeed);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Cannot initialize random seed", e);
		}
	}

	@Override
	public void startGame(ECSGame game) {
		game.getEntitiesWithComponent(AIComponent.class).forEach(e -> e.getComponent(AIComponent.class).setPaused(true));
		game.getEvents().registerHandlerAfter(this, StartGameEvent.class, e -> this.onStart(e, game));
		
		Set<Entity> players = game.getEntitiesWithComponent(PlayerComponent.class);
		players.forEach(this::giveReplayControls);
	}
	
	private void onStart(StartGameEvent event, ECSGame game) {
		 game.findSystemsOfClass(AISystem.class).forEach(game::removeSystem);
		 game.addSystem(new NextStepReplayActionSystem());
	}
	
	private static class NextStepReplayActionSystem extends SpecificActionSystem {
		public NextStepReplayActionSystem() {
			super(NEXT_STEP);
		}

		@Override
		protected void isAllowed(ActionAllowedCheckEvent event) {
			event.setAllowed(event.getEntity() == event.getPerformer());
		}
		
		@Override
		protected void onPerform(ActionPerformEvent event) {
		}
	}
	
	private void giveReplayControls(Entity player) {
		ActionComponent actions = player.getComponent(ActionComponent.class);
		if (actions == null) {
			actions = new ActionComponent();
			player.addComponent(actions);
		}
		actions.addAction(new ECSAction(player, NEXT_STEP, e -> true, this::nextStep));
	}
	
	private void nextStep(ECSAction replayAction) {
		if (currentActionIndex >= replayData.getActionInformation().size()) {
			return;
		}
		ReplayAction step = replayData.getActionInformation().get(currentActionIndex);
		ECSGame game = replayAction.getOwner().getGame();
		Entity entity = game.getEntity(step.getEntity());
		ECSAction action = Actions.getAction(entity, step.getActionName());
		if (action.getTargetSets().size() != step.getTargets().size()) {
			throw new RuntimeException("Replay targetsets does not match actual");
		}
		
		for (int i = 0; i < action.getTargetSets().size(); i++) {
			TargetSet targetSet = action.getTargetSets().get(i);
			List<Integer> targets = step.getTargets().get(i);
			
			targetSet.clearTargets();
			for (int target : targets) {
				targetSet.addTarget(game.getEntity(target));
			}
		}
		
		boolean performSuccess = action.perform(game.getEntity(step.getPerformer()));
		if (!performSuccess) {
			System.out.println("WARNING: Replay action not correctly performed " + action);
		}
		currentActionIndex++;
	}

	public void setPlayerConfigs(ECSGame game) {
		for (Entry<Integer, PlayerConfigMessage> storedConfig : replayData.getEntityConfigs().entrySet()) {
			System.out.println("Processing stored config " + storedConfig.getKey() + ": " + storedConfig.getValue());
			Entity entity = game.getEntity(storedConfig.getKey());
			ConfigComponent config = entity.getComponent(ConfigComponent.class);
			for (Entry<String, Object> configuration : storedConfig.getValue().getConfigs().entrySet()) {
				System.out.println("Adding config " + configuration.getKey() + " of type " + configuration.getValue().getClass() + ": " + configuration.getValue());
				config.addConfig(configuration.getKey(), configuration.getValue());
			}
			config.setConfigured(true);
		}
	}

}
