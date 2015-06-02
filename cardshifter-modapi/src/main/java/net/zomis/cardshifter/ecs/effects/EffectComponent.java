package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.CopyableComponent;
import com.cardshifter.modapi.base.Entity;

public class EffectComponent extends Component implements CopyableComponent {

	private final GameEffect effect;
	private final String description;

	public EffectComponent(String description, GameEffect effect) {
		this.description = description;
		this.effect = effect;
	}
	
	public void perform(ActionPerformEvent event) {
		effect.accept(event.getEntity(), event);
	}
	
    public void perform(Entity entity) {
        effect.accept(entity, null);
    }

	public String getDescription() {
		return description;
	}

	@Override
	public Component copy(Entity copyTo) {
		return new EffectComponent(description, effect);
	}

	public Component and(EffectComponent next) {
		return new EffectComponent(description + "\n" + next.description,
                (entity, event) -> effect.andThen(next.effect).accept(entity, event));
	}

	public GameEffect getEffect() {
		return effect;
	}
}
