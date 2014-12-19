package com.cardshifter.gdx.api.outgoing;

import com.cardshifter.gdx.api.messages.Message;

import java.util.Arrays;

public class AvailableModsMessage extends Message {

	private final String[] mods;

	public AvailableModsMessage(String[] mods) {
		super("availableMods");
		this.mods = Arrays.copyOf(mods, mods.length);
	}
	
	public String[] getMods() {
		return Arrays.copyOf(mods, mods.length);
	}

	@Override
	public String toString() {
		return "AvailableModsMessage [mods=" + Arrays.toString(mods) + "]";
	}
	
}
