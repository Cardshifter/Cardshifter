package com.cardshifter.server.incoming;

import com.cardshifter.server.abstr.CardMessage;


public class UseAbilityMessage extends CardMessage {

	public UseAbilityMessage() {
		super("use");
	}
	// { "command": "useAbility", "card": "123abc", "ability": "poke" }

}
