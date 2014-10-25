package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.base.Retriever;
import com.cardshifter.modapi.base.Retrievers;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.Cards;

public class Filters {

	@Retriever private ComponentRetriever<CreatureTypeComponent> creature = Retrievers.component(CreatureTypeComponent.class);
	
	public TargetFilter isCreature() {
		return (src, target) -> creature.has(target);
	}
	
	public TargetFilter isOnBattlefield() {
		return (src, target) -> Cards.isOnZone(target, BattlefieldComponent.class);
	}
	
	public TargetFilter friendly() {
		return (src, target) -> Cards.isCard(target) && Cards.getOwner(src) == Cards.getOwner(target);
	}
	
	public TargetFilter enemy() {
		return (src, target) -> Cards.isCard(target) && Cards.getOwner(src) != Cards.getOwner(target);
	}
	
	public TargetFilter isCreatureOnBattlefield() {
		return isCreature().and(isOnBattlefield());
	}
	
}
