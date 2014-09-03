package com.cardshifter.server.outgoing;

import java.util.Collections;
import java.util.Map;

import com.cardshifter.core.LuaTools;
import com.cardshifter.core.Player;
import com.cardshifter.server.incoming.Message;

public class PlayerMessage extends Message {
//	SERVER: command: player, name: 'Bubu', properties: { hp: 23 }

	private final String name;
	private final Map<String, String> properties;

	public PlayerMessage(Player playerFor) {
		super("player");
		this.name = playerFor.getName();
		this.properties = LuaTools.tableToJava(playerFor.data);
	}

	public String getName() {
		return name;
	}
	
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	
}
