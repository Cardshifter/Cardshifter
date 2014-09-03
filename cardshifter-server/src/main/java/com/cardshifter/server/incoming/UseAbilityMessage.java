package com.cardshifter.server.incoming;


public class UseAbilityMessage extends CardMessage {

	public UseAbilityMessage() {
		super("use");
	}
	// { "command": "useAbility", "card": "123abc", "ability": "poke" }

}
