package com.cardshift.core;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class Action {

	private final String name;
	private final LuaValue allowedFunction;
	private final LuaValue actionFunction;

	public Action(String name, LuaValue allowedFunction, LuaValue actionFunction) {
		if (!allowedFunction.isfunction()) {
			throw new IllegalArgumentException("Must specify a function for determining if action is allowed");
		}
		if (!actionFunction.isfunction()) {
			throw new IllegalArgumentException("Must specify an action function");
		}
		this.name = name;
		this.allowedFunction = allowedFunction;
		this.actionFunction = actionFunction;
	}
	
	public LuaValue getActionFunction() {
		return actionFunction;
	}
	
	public LuaValue getAllowedFunction() {
		return allowedFunction;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isAllowed(Card card) {
		return allowedFunction.invoke(CoerceJavaToLua.coerce(card)).arg1().toboolean();
	}
	
	public void perform(Card card) {
		actionFunction.invoke(CoerceJavaToLua.coerce(card));
	}
}
