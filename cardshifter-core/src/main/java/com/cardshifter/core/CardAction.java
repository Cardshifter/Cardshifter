package com.cardshifter.core;

import java.util.Objects;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class CardAction extends UsableAction {
	private final Card card;

	public CardAction(final Card card, final String name, final LuaValue allowedFunction, final LuaValue actionFunction) {
		super(name, allowedFunction, actionFunction);
		this.card = Objects.requireNonNull(card, "card");
	}
	
	public Card getCard() {
		return card;
	}

	@Override
	protected LuaValue methodArg() {
		return CoerceJavaToLua.coerce(card);
	}

	@Override
	protected Game getGame() {
		return card.getGame();
	}
	
	@Override
	public String toString() {
		return "{Action " + getName() + " on card " + card + "}";
	}
}
