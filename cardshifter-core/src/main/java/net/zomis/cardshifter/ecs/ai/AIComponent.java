package net.zomis.cardshifter.ecs.ai;

import java.util.Objects;

import com.cardshifter.ai.CardshifterAI;

import net.zomis.cardshifter.ecs.base.Component;

public class AIComponent extends Component {
	
	private CardshifterAI ai;

	public AIComponent(CardshifterAI ai) {
		setAI(ai);
	}
	
	public void setAI(CardshifterAI ai) {
		this.ai = Objects.requireNonNull(ai);
	}
	
	public CardshifterAI getAI() {
		return ai;
	}
	
}
