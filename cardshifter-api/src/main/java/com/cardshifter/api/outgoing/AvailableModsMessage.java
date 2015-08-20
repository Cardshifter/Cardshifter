package com.cardshifter.api.outgoing;

import java.util.Arrays;

import com.cardshifter.api.ArrayUtil;
import com.cardshifter.api.messages.Message;
/**
 * Server sending list of available mods to client.
 */
public class AvailableModsMessage extends Message {
	
	private String[] mods;
	/** Constructor. (no params) */
	public AvailableModsMessage() {
		this(new String[]{ "N/A" });
	}
	/**
	 * Constructor.
	 * @param mods  Array containing all available mods.
	 */
	public AvailableModsMessage(String[] mods) {
		super("availableMods");
		this.mods = ArrayUtil.copyOf(mods);
	}
	/** @return Array containing all available mods.  */
	public String[] getMods() {
		return ArrayUtil.copyOf(mods);
	}
	/** @return  This message as converted to String */
	@Override
	public String toString() {
		return "AvailableModsMessage [mods=" + Arrays.toString(mods) + "]";
	}
	
}
