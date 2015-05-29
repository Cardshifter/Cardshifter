package com.cardshifter.core.replays;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.cardshifter.api.config.PlayerConfig;
import net.zomis.cardshifter.ecs.config.ConfigComponent;

import com.cardshifter.api.both.PlayerConfigMessage;
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

	private final ECSGame game;

	public ReplayPlaybackSystem(ECSGame game, ReplayRecordSystem replay) {
		this.replayData = replay;
		this.game = game;
		applySeed(game, replay.getSeed());
	}

	private void applySeed(ECSGame game, long newSeed) {
		Random random = game.getRandom();
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
		
		List<Entity> players = new ArrayList<>(game.getEntitiesWithComponent(PlayerComponent.class));
//		players.sort(Comparator.comparing(pl -> pl.getComponent(PlayerComponent.class).getIndex()));
		players.forEach(this::setPlayerName);
		players.forEach(this::giveReplayControls);
	}
	
	private void setPlayerName(Entity playerEntity) {
		PlayerComponent playerInfo = playerEntity.getComponent(PlayerComponent.class);
		int index = playerInfo.getIndex();
		if (this.replayData.getPlayerNames() != null) {
			playerInfo.setName(this.replayData.getPlayerNames().get(index));
		}
	}
	
	private void onStart(StartGameEvent event, ECSGame game) {
		game.findSystemsOfClass(ReplayRecordSystem.class).forEach(game::removeSystem);
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
		if (isReplayFinished()) {
			return;
		}
		nextStep();
	}

	public boolean isReplayFinished() {
		return currentActionIndex >= replayData.getActionInformation().size();
	}

	public void nextStep() {
		ReplayAction step = replayData.getActionInformation().get(currentActionIndex);
		Entity entity = game.getEntity(step.getEntity());
		ECSAction action = Actions.getAction(entity, step.getActionName());
		if (action.getTargetSets().size() != step.getTargets().size()) {
			throw new ReplayException("Number of targetsets does not match for action " + action + " at action index " + currentActionIndex);
		}
		
		for (int i = 0; i < action.getTargetSets().size(); i++) {
			TargetSet targetSet = action.getTargetSets().get(i);
			List<Integer> targets = step.getTargets().get(i);
			
			targetSet.clearTargets();
			for (int target : targets) {
				Entity targetEntity = game.getEntity(target);
				if (targetEntity == null) {
					throw new ReplayException("Target " + target + " not found when performing " + action + " at action index " + currentActionIndex);
				}
				targetSet.addTarget(targetEntity);
			}
		}
		
		boolean performSuccess = action.perform(game.getEntity(step.getPerformer()));
		if (!performSuccess) {
			throw new ReplayException("Replay action not correctly performed " + action + " at action index " + currentActionIndex);
		}
		currentActionIndex++;
	}

	public void setPlayerConfigs(ECSGame game) {
		for (Entry<Integer, PlayerConfigMessage> storedConfig : replayData.getEntityConfigs().entrySet()) {
			System.out.println("Processing stored config " + storedConfig.getKey() + ": " + storedConfig.getValue());
			Entity entity = game.getEntity(storedConfig.getKey());
			ConfigComponent config = entity.getComponent(ConfigComponent.class);
			for (Entry<String, PlayerConfig> configuration : storedConfig.getValue().getConfigs().entrySet()) {
				System.out.println("Adding config " + configuration.getKey() + " of type " + configuration.getValue().getClass() + ": " + configuration.getValue());
				config.addConfig(configuration.getKey(), configuration.getValue());
			}
			config.setConfigured(true);
		}
	}

}
