package com.cardshifter.core.actions;

import java.util.Objects;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.cardshifter.core.Events;
import com.cardshifter.core.Game;

@Deprecated
public abstract class UsableAction implements Action {
	private final String name;
	private final LuaFunction isAllowedFunction;
	private final LuaFunction performFunction;
	
	public UsableAction(final String name, final LuaValue isAllowedFunction, final LuaValue performFunction) {
		this.name = Objects.requireNonNull(name, "name");
		this.isAllowedFunction = isAllowedFunction.checkfunction();
		this.performFunction = performFunction.checkfunction();
	}
	
	public LuaFunction getIsAllowedFunction() {
		return isAllowedFunction;
	}

	public LuaFunction getPerformFunction() {
		return performFunction;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean isAllowed() {
		return isAllowedFunction.invoke(methodArg()).arg1().toboolean();
	}
	
	protected abstract LuaValue methodArg();

	@Override
	public void perform() {
		Game game = getGame(); // stored here in case it is unavailable after action has been performed
		getPerformFunction().invoke(methodArg());
		game.getEvents().callEvent(Events.ACTION_USED, methodArg(), CoerceJavaToLua.coerce(this));
	}

	protected abstract Game getGame();
	
	public abstract int getEntityId();
	
}
