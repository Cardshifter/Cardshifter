package com.cardshifter.modapi.ai;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

import com.cardshifter.modapi.base.Component;

public class AIComponent extends Component {
	
	private CardshifterAI ai;
	private long delay = 4000;
	private boolean paused;
	ScheduledFuture<?> future;

	public AIComponent(CardshifterAI ai) {
		setAI(ai);
	}
	
	public void setAI(CardshifterAI ai) {
		this.ai = Objects.requireNonNull(ai);
	}
	
	public CardshifterAI getAI() {
		return ai;
	}

	public long getDelay() {
		return delay;
	}
	
	public void setDelay(long delay) {
		this.delay = delay;
	}

	public boolean hasWaitingAction() {
		return future != null && !future.isDone();
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
}
