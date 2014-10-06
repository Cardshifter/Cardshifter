package net.zomis.cardshifter.ecs.base;

public class GameOverEvent implements CancellableEvent {

	private boolean cancelled;
	private final ECSGame game;

	public GameOverEvent(ECSGame game) {
		this.game = game;
	}
	
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	public ECSGame getGame() {
		return game;
	}
	
}
