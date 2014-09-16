package net.zomis.cardshifter.ecs.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.zomis.cardshifter.ecs.base.Component;

public class ActionComponent extends Component {

	private final Map<String, ECSAction> actions = new HashMap<>();

	public ActionComponent() {
	}
	
	public ActionComponent addAction(ECSAction action) {
		this.actions.put(action.getName(), action);
		return this;
	}

	public Set<String> getActions() {
		return actions.keySet();
	}

	public ECSAction getAction(String key) {
		return this.actions.get(key);
	}

	public Collection<ECSAction> getECSActions() {
		return new ArrayList<>(actions.values());
	}
	
}
