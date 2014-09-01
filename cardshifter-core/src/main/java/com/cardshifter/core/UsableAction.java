package com.cardshifter.core;

import java.util.Objects;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public abstract class UsableAction implements Action {
	private final String name;
	private final LuaFunction allowedFunction;
	private final LuaFunction actionFunction;
	
	public UsableAction(final String name, final LuaValue allowedFunction, final LuaValue actionFunction) {
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
	
	@Override
	public boolean isAllowed() {
		return allowedFunction.invoke(methodArg()).arg1().toboolean();
	}
	
	protected abstract LuaValue methodArg();

	@Override
	public void perform() {
		Game game = getGame(); // stored here in case it is unavailable after action has been performed
		getActionFunction().invoke(methodArg());
		game.getEvents().callEvent(Events.ACTION_USED, methodArg(), CoerceJavaToLua.coerce(this));
	}

	protected abstract Game getGame();
}
