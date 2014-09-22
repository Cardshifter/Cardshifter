package com.cardshifter.server.outgoing;

import java.util.Arrays;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AvailableTargetsMessage extends Message {

	private final int entity;
	private final String action;
	private final int min;
	private final int max;
	private final int[] targets;

	@JsonCreator
	public AvailableTargetsMessage(@JsonProperty("entity") int entity, @JsonProperty("action") String action,
			@JsonProperty("targets") int[] targets, 
			@JsonProperty("min") int min, @JsonProperty("max") int max) {
		super("targets");
		this.entity = entity;
		this.action = action;
		this.min = min;
		this.max = max;
		this.targets = Arrays.copyOf(targets, targets.length);
	}
	
	public int getMax() {
		return max;
	}
	
	public int getMin() {
		return min;
	}
	
	public int[] getTargets() {
		return targets;
	}

	public String getAction() {
		return action;
	}
	
	public int getEntity() {
		return entity;
	}
	
}
