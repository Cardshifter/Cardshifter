package com.cardshifter.modapi.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.CopyableComponent;
import com.cardshifter.modapi.base.Entity;

public class ActionComponent extends Component implements CopyableComponent {

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
	
	public boolean removeAction(String actionName) {
		return actions.remove(actionName) != null;
	}

	@Override
	public Component copy(Entity copyTo) {
		ActionComponent copy = new ActionComponent();
		for (Entry<String, ECSAction> entry : actions.entrySet()) {
			copy.addAction(entry.getValue().copy(copyTo));
		}
		return copy;
	}

	@Override
	public String toString() {
		return "ActionComponent [actions=" + actions + "]";
	}
	
}
