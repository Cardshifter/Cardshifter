package com.cardshifter.api.outgoing;

import com.cardshifter.api.ArrayUtil;
import com.cardshifter.api.messages.Message;

import java.util.Arrays;

/**
 * Send available targets to client.
 * <p>
 * Many game actions are performed on targets. 
 * This message sends available targets to the client so that the player entity can select from among them.
 * See also: api/incoming/RequestTargetsMessage.java
 */
public class AvailableTargetsMessage extends Message {

	private int entity;
	private String action;
	private int min;
	private int max;
	private int[] targets;
	
	/** Constructor. (no params) */
	public AvailableTargetsMessage() {
		this(0, "", new int[0], 0, 0);
	}
	/**
	 * Constructor.
	 * @param entity  Id of this entity
	 * @param action  Name of this action
	 * @param targets  Set of targets available for this entity and this action
	 * @param min  Minimum number of targets
	 * @param max  Maximum number of targets
	 */
	public AvailableTargetsMessage(int entity, String action, int[] targets, int min, int max) {
		super("targets");
		this.entity = entity;
		this.action = action;
		this.min = min;
		this.max = max;
		this.targets = ArrayUtil.copyOf(targets);
	}
	/** @return  Maximum number of targets */
	public int getMax() {
		return max;
	}
	/** @return  Minimum number of targets */
	public int getMin() {
		return min;
	}
	/** @return  Set of targets available for this entity and this action */
	public int[] getTargets() {
		return ArrayUtil.copyOf(targets);
	}
	/** @return  Name of this action */
	public String getAction() {
		return action;
	}
	/** @return  Id of this entity */
	public int getEntity() {
		return entity;
	}
	/** @return  The name of this action requested to be performed */
	@Override
	public String toString() {
		return "AvailableTargetsMessage [entity=" + entity + ", action="
				+ action + ", min=" + min + ", max=" + max + ", targets="
				+ Arrays.toString(targets) + "]";
	}
	
}
