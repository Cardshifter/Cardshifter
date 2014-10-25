package com.cardshifter.core.replays;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import net.zomis.cardshifter.ecs.usage.CardshifterIO;
import net.zomis.cardshifter.ecs.usage.ConfigComponent;

import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.events.GameOverEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

public class ReplayRecordSystem implements ECSSystem {

	@JsonProperty
	private final List<ReplayAction> actionInformation = new ArrayList<>();
	
	@JsonProperty
	@JsonTypeInfo(include = As.PROPERTY, use = Id.NAME, property = "_type")
	private final Map<Integer, PlayerConfigMessage> entityConfigs = new HashMap<>();
	
	private final long seed;
	private final File file;
	
	ReplayRecordSystem(@JsonProperty("seed") long seed) {
		this.seed = seed;
		this.file = null;
	}
	
	public ReplayRecordSystem(ECSGame game, File output) {
		this.seed = fetchSeed(game.getRandom());
		this.file = output;
	}

	private long fetchSeed(Random random) {
		try {
			Field field = random.getClass().getDeclaredField("seed");
			field.setAccessible(true);
			Object result = field.get(random);
			AtomicLong seed = (AtomicLong) result;
			return seed.get();
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("Cannot initialize random seed", e);
		}
	}

	@Override
	public void startGame(ECSGame game) {
		if (file == null) {
			return;
		}
		game.getEvents().registerHandlerAfter(this, ActionPerformEvent.class, this::recordAction);
		game.getEvents().registerHandlerAfter(this, GameOverEvent.class, this::saveReplay);
		
		Set<Entity> configs = game.getEntitiesWithComponent(ConfigComponent.class);
		for (Entity configEntity : configs) {
			ConfigComponent config = configEntity.getComponent(ConfigComponent.class);
			entityConfigs.put(configEntity.getId(), new PlayerConfigMessage(0, config.getConfigs()));
		}
		
	}
	
	private void recordAction(ActionPerformEvent event) {
		actionInformation.add(ReplayAction.forAction(event));
	}
	
	private void saveReplay(GameOverEvent event) {
		try {
			CardshifterIO.mapper().writeValue(file, this);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save replay", e);
		}
	}
	
	public long getSeed() {
		return seed;
	}
	
	public List<ReplayAction> getActionInformation() {
		return actionInformation;
	}
	
	public Map<Integer, PlayerConfigMessage> getEntityConfigs() {
		return Collections.unmodifiableMap(entityConfigs);
	}
	
}
