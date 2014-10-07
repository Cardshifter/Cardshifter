package com.cardshifter.api.outgoing;

import java.util.Arrays;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AvailableModsMessage extends Message {

	private final String[] mods;

	@JsonCreator
	public AvailableModsMessage(@JsonProperty("mods") String[] mods) {
		super("availableMods");
		this.mods = Arrays.copyOf(mods, mods.length);
	}
	
	public String[] getMods() {
		return Arrays.copyOf(mods, mods.length);
	}

}
