package net.zomis.cardshifter.ecs.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.zomis.cardshifter.ecs.base.Component;
import net.zomis.cardshifter.ecs.base.ECSAction;

public class ActionComponent implements Component {

	private final Map<String, ECSAction> actions = new HashMap<>();

	public ActionComponent() {
	}
	
	public void addAction(ECSAction action) {
		this.actions.put(action.getName(), action);
	}

	public Set<String> getActions() {
		return actions.keySet();
	}

	public ECSAction getAction(String key) {
		return this.actions.get(key);
	}
	
}
