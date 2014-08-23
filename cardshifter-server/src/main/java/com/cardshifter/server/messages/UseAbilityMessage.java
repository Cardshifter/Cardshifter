package com.cardshifter.server.messages;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.annotation.JacksonInject;

public class UseAbilityMessage extends CardMessage {

	public UseAbilityMessage(@JacksonInject Server server) {
		super(server);
	}
	// { "command": "useAbility", "card": "123abc", "ability": "poke" }

	@Override
	public void perform(ClientIO client) {
		// TODO Auto-generated method stub
		
	}
}
