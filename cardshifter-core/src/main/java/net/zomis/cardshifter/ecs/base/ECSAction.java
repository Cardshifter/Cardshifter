package net.zomis.cardshifter.ecs.base;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ECSAction {

	private final Entity owner;
	private final String name;

	private final Predicate<ECSAction> allowed;
	private final Consumer<ECSAction> perform;
	
	public ECSAction(Entity owner, String name, Predicate<ECSAction> allowed, Consumer<ECSAction> perform) {
		this.owner = owner;
		this.name = name;
		this.allowed = allowed;
		this.perform = perform;
	}
	
	public String getName() {
		return name;
	}
	
	public Entity getOwner() {
		return owner;
	}
	
	public ECSAction copy() {
		return new ECSAction(this.owner, this.name, this.allowed, this.perform);
	}

	public void perform() {
		if (this.isAllowed()) {
			this.perform.accept(this);
		}
	}

	private boolean isAllowed() {
		return this.allowed.test(this);
	}
	
}
