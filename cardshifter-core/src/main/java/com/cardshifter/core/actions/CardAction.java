package com.cardshifter.core.actions;

import java.util.Objects;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.cardshifter.core.Card;
import com.cardshifter.core.Game;

@Deprecated
public class CardAction extends UsableAction {
	private final Card card;

	public CardAction(final Card card, final String name, final LuaValue isAllowedFunction, final LuaValue performFunction) {
		super(name, isAllowedFunction, performFunction);
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
	public int getEntityId() {
		return card.getId();
	}
	
	@Override
	public String toString() {
		return "{Action " + getName() + " on card " + card + "}";
	}
}
