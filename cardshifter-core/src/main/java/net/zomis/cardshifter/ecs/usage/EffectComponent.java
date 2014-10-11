package net.zomis.cardshifter.ecs.usage;

import java.util.function.Consumer;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.Entity;

public class EffectComponent extends Component {

	private final Consumer<Entity> effect;

	public EffectComponent(Consumer<Entity> effect) {
		this.effect = effect;
	}
	
	public void perform(Entity entity) {
		effect.accept(entity);
	}

}
