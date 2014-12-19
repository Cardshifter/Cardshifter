package com.cardshifter.gdx.api.outgoing;

import com.cardshifter.gdx.api.messages.Message;

import java.util.Arrays;

public class AvailableTargetsMessage extends Message {

	private final int entity;
	private final String action;
	private final int min;
	private final int max;
	private final int[] targets;

	public AvailableTargetsMessage(int entity, String action,
			int[] targets,
			int min, int max) {
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
		return Arrays.copyOf(targets, targets.length);
	}

	public String getAction() {
		return action;
	}
	
	public int getEntity() {
		return entity;
	}

	@Override
	public String toString() {
		return "AvailableTargetsMessage [entity=" + entity + ", action="
				+ action + ", min=" + min + ", max=" + max + ", targets="
				+ Arrays.toString(targets) + "]";
	}
	
}
