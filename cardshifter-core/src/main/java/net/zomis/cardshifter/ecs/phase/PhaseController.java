package net.zomis.cardshifter.ecs.phase;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.Predicate;

import net.zomis.cardshifter.ecs.base.Component;

public class PhaseController extends Component {
	
	private final LinkedList<Phase> upcomingPhases = new LinkedList<>();
	private final LinkedList<Phase> permanentPhases = new LinkedList<>();
	
	public PhaseController() {
	}

	public void addPhase(Phase phase) {
		permanentPhases.add(phase);
		upcomingPhases.add(phase);
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
		upcomingPhases.removeFirst();
		executeEvent(new PhaseStartEvent(this, oldPhase, getCurrentPhase()));
		
		return getCurrentPhase();
	}
	
}
