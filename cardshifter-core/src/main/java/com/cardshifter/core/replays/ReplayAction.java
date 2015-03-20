package com.cardshifter.core.replays;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.cardshifter.modapi.actions.ActionPerformEvent;

public class ReplayAction {

	private final int entity;
	private final List<List<Integer>> targets;
	private final int performer;
	private final String actionName;
	
	ReplayAction() {
		this.entity = 0;
		this.targets = null;
		this.performer = 0;
		this.actionName = "N/A";
	}
	
	private ReplayAction(ActionPerformEvent event) {
		this.entity = event.getEntity().getId();
		this.performer = event.getPerformer().getId();
		this.actionName = event.getAction().getName();
		this.targets = event.getAction().getTargetSets().stream()
			.map(targetSet -> targetSet.getChosenTargets().stream()
				.map(e -> e.getId()).collect(Collectors.toList()))
			.collect(Collectors.toList());
	}

	public int getEntity() {
		return entity;
	}
	
	public String getActionName() {
		return actionName;
	}
	
	public int getPerformer() {
		return performer;
	}
	
	public List<List<Integer>> getTargets() {
		return Collections.unmodifiableList(targets);
	}

    @Override
    public String toString() {
        return actionName + " on " + entity + " by " + performer + " with targets " + targets;
    }

    public static ReplayAction forAction(ActionPerformEvent event) {
		ReplayAction act = new ReplayAction(event);
		return act;
	}
	
}
