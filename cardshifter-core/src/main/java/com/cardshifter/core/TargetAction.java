package com.cardshifter.core;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class TargetAction extends UsableAction {
	private final Card card;
	private final LuaValue targetAllowed;

	public TargetAction(final Card card, final String name, final LuaValue actionAllowed, final LuaValue targetAllowed, final LuaValue actionPerformed) {
		super(name, actionAllowed, actionPerformed);
		this.card = card;
		this.targetAllowed = targetAllowed;
	}

	@Override
	public void perform() {
		// I know, this stinks.
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected LuaValue methodArg() {
		return CoerceJavaToLua.coerce(card);
	}

	@Override
	protected Game getGame() {
		return card.getGame();
	}

	public List<Targetable> findTargets() {
		Game game = getGame();
		
		Stream<? extends Targetable> targetablePlayers = game.getPlayers().stream()
			.filter(this::isValidTarget);
		
		Stream<? extends Targetable> targetableCards = game.getZones().stream()
			.flatMap(zone -> zone.getCards().stream())
			.filter(this::isValidTarget);
		
		return Stream.concat(targetablePlayers, targetableCards).collect(Collectors.toList());
	}

	private boolean isValidTarget(final Targetable target) {
		return targetAllowed.invoke(CoerceJavaToLua.coerce(card), CoerceJavaToLua.coerce(target), CoerceJavaToLua.coerce(this)).arg1().toboolean();
	}

	public void perform(final Targetable target) {
		Game game = getGame(); // stored here in case it is unavailable after action has been performed
		getActionFunction().invoke(CoerceJavaToLua.coerce(card), CoerceJavaToLua.coerce(target), CoerceJavaToLua.coerce(this));
		game.getEvents().callEvent(Events.ACTION_USED, CoerceJavaToLua.coerce(card), CoerceJavaToLua.coerce(this));
	}
	
	@Override
	public String toString() {
		return "{TargetAction " + this.getName() + " on card " + this.card + "}";
	}
}
