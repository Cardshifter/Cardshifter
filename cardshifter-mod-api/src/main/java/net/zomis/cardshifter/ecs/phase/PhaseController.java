package net.zomis.cardshifter.ecs.phase;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.Predicate;

import net.zomis.cardshifter.ecs.base.Component;
import net.zomis.cardshifter.ecs.base.Entity;

public class PhaseController extends Component {
	
	private final LinkedList<Phase> upcomingPhases = new LinkedList<>();
	private final LinkedList<Phase> permanentPhases = new LinkedList<>();
	private int phaseNumber;
	private int recreateCount;
	
	public PhaseController() {
	}

	public PhaseController addPhase(Phase phase) {
		permanentPhases.add(phase);
		upcomingPhases.add(phase);
		return this;
	}
	
	public void insertTemporaryPhaseNext(Phase phase) {
		upcomingPhases.add(1, phase);
	}
	
	public boolean insertTemporaryPhaseBefore(Phase phase, Predicate<Phase> beforePhase) {
		ListIterator<Phase> it = navigateToRecreate(beforePhase);
		if (it != null) {
			if (it.previousIndex() >= 0) {
				it.previous();
			}
			it.add(phase);
		}
		return it != null;
	}
	
	private ListIterator<Phase> navigateToRecreate(Predicate<Phase> after) {
		ListIterator<Phase> it = navigateTo(upcomingPhases.listIterator(), after);
		if (it == null) {
			int size = upcomingPhases.size();
			upcomingPhases.addAll(permanentPhases);
			it = navigateTo(upcomingPhases.listIterator(size), after);
			if (it == null) {
				return null;
			}
		}
		return it;
	}

	private static <T> ListIterator<T> navigateTo(ListIterator<T> iterator, Predicate<T> navigateAfter) {
		ListIterator<T> it = iterator;
		while (it.hasNext()) {
			T next = it.next();
			if (navigateAfter.test(next)) {
				return it;
			}
		}
		return null;
	}

	public boolean insertTemporaryPhaseAfter(Phase phase, Predicate<Phase> afterPhase) {
		ListIterator<Phase> it = navigateToRecreate(afterPhase);
		if (it != null) {
			it.add(phase);
		}
		return it != null;
	}
	
	public Phase getCurrentPhase() {
		refillPhases();
		return upcomingPhases.getFirst();
	}
	
	private void refillPhases() {
		if (upcomingPhases.isEmpty()) {
			upcomingPhases.addAll(permanentPhases);
		}
	}

	public Phase nextPhase() {
		Phase oldPhase = getCurrentPhase();
		executeEvent(new PhaseEndEvent(this, oldPhase));
		phaseNumber++;
		upcomingPhases.removeFirst();
		Phase currentPhase = getCurrentPhase();
		if (currentPhase == permanentPhases.peekFirst()) {
			recreateCount++;
		}
		executeEvent(new PhaseStartEvent(this, oldPhase, currentPhase));
		
		return currentPhase;
	}

	public Entity getCurrentEntity() {
		return getCurrentPhase().getOwner();
	}
	
	/**
	 * Get the individual phase number. Increased with each call to {@link #nextPhase()}
	 * @return
	 */
	public int getPhaseNumber() {
		return phaseNumber;
	}
	
	/**
	 * Return the number of loops that has been made. Increased whenever {@link #nextPhase()} starts on the first permanent phase.
	 * @return
	 */
	public int getRecreateCount() {
		return recreateCount;
	}

	public void insertTemporaryPhaseBeforeCurrent(Phase phase) {
		this.upcomingPhases.addFirst(phase);
	}
	
}
