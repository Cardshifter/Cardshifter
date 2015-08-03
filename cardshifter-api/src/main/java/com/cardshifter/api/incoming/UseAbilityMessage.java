package com.cardshifter.api.incoming;

import java.util.Arrays;

import com.cardshifter.api.ArrayUtil;
import com.cardshifter.api.abstr.CardMessage;

/**
 * Message for a game entity to use a certain ability.
 * <p>
 * Game entities (e.g., cards, players) may have one or more ability actions that they can perform.
 * Certain abilities can have multiple targets, hence the use of an array. 
 */
public class UseAbilityMessage extends CardMessage {

	private final int id;
	private final String action;
	private final int gameId;
	private final int[] targets;
	
	/** Constructor. (no params) */
	public UseAbilityMessage() {
		this(0, 0, "", new int[0]);
	}

	/**
	 * Constructor. (multiple targets)
	 * <p>
	 * Used for multiple target actions.
	 * 
	 * @param gameId  This current game
	 * @param entity  This game entity performing an action
	 * @param action  This action
	 * @param targets  The set of multiple targets affected by this action
	 */
	public UseAbilityMessage(int gameId, int id, String action, int[] targets) {
		super("use");
		this.id = id;
		this.action = action;
		this.gameId = gameId;
		this.targets = ArrayUtil.copyOf(targets);
	}
	/**
	 * Constructor. 
	 * <p>
	 * Used for single target actions.
	 * 
	 * @param gameId  This current game
	 * @param entity  This game entity performing an action
	 * @param action  This action
	 * @param target  The single target affected by this action
	 */
	public UseAbilityMessage(int gameid, int entity, String action, int target) {
		this(gameid, entity, action, new int[]{ target });
	}
	
	/** @return  This action */
	public String getAction() {
		return action;
	}
	/** @return  This game entity performing an action */
	public int getId() {
		return id;
	}
	/** @return  This current game */
	public int getGameId() {
		return gameId;
	}
	/** @return  This set of targets */
	public int[] getTargets() {
		return ArrayUtil.copyOf(targets);
	}
	/** @return  This request as converted to String */
	@Override
	public String toString() {
		return "UseAbilityMessage ["
				+ "id=" + id 
				+ ", action=" + action
				+ ", gameId=" + gameId 
				+ ", targets=" + Arrays.toString(targets) 
			+ "]";
	}

}
