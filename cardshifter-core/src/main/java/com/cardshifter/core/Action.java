package com.cardshifter.core;

import java.util.Objects;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class Action {
	private final String name;
	private final LuaFunction allowedFunction;
	private final LuaFunction actionFunction;
	private final Card card;

	public Action(final Card card, final String name, final LuaValue allowedFunction, final LuaValue actionFunction) {
		this.card = Objects.requireNonNull(card, "card");
		this.name = Objects.requireNonNull(name, "name");
		this.allowedFunction = allowedFunction.checkfunction();
		this.actionFunction = actionFunction.checkfunction();
	}
	
	public LuaFunction getActionFunction() {
		return actionFunction;
	}
	
	public LuaFunction getAllowedFunction() {
		return allowedFunction;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isAllowed() {
		return allowedFunction.invoke(CoerceJavaToLua.coerce(card)).arg1().toboolean();
	}
	
	public void perform() {
		actionFunction.invoke(CoerceJavaToLua.coerce(card));
	}
	
	public Card getCard() {
		return card;
	}
	
	@Override
	public String toString() {
		return "{Action " + name + " on card " + card + "}";
	}
}
