package com.cardshifter.api.incoming;

import java.util.Arrays;

import com.cardshifter.api.ArrayUtil;
import com.cardshifter.api.abstr.CardMessage;

public class UseAbilityMessage extends CardMessage {

	private final int id;
	private final String action;
	private final int gameId;
	private final int[] targets;

	public UseAbilityMessage() {
		this(0, 0, "", new int[0]);
	}

	public UseAbilityMessage(int gameId, int id, String action, int[] targets) {
		super("use");
		this.id = id;
		this.action = action;
		this.gameId = gameId;
		this.targets = ArrayUtil.copyOf(targets);
	}
	
	public UseAbilityMessage(int gameid, int entity, String action, int target) {
		this(gameid, entity, action, new int[]{ target });
	}

	public String getAction() {
		return action;
	}
	
	public int getId() {
		return id;
	}
	
	public int getGameId() {
		return gameId;
	}
	
	public int[] getTargets() {
		return ArrayUtil.copyOf(targets);
	}

	@Override
	public String toString() {
		return "UseAbilityMessage [id=" + id + ", action=" + action
				+ ", gameId=" + gameId + ", targets=" + Arrays.toString(targets) + "]";
	}

}
