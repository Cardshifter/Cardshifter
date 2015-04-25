package com.cardshifter.api.outgoing;

import java.util.Arrays;

import com.cardshifter.api.ArrayUtil;
import com.cardshifter.api.messages.Message;

public class AvailableModsMessage extends Message {

	private final String[] mods;

	public AvailableModsMessage() {
		this(new String[]{ "N/A" });
	}
	
	public AvailableModsMessage(String[] mods) {
		super("availableMods");
		this.mods = ArrayUtil.copyOf(mods);
	}
	
	public String[] getMods() {
		return ArrayUtil.copyOf(mods);
	}

	@Override
	public String toString() {
		return "AvailableModsMessage [mods=" + Arrays.toString(mods) + "]";
	}
	
}
