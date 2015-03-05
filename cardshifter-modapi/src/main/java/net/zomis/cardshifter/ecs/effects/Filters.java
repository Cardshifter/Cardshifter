package net.zomis.cardshifter.ecs.effects;

import com.cardshifter.modapi.base.*;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.players.Players;

public class Filters {

	@Retriever private ComponentRetriever<CreatureTypeComponent> creature = Retrievers.component(CreatureTypeComponent.class);
	
	public TargetFilter isCreature() {
		return (src, target) -> creature.has(target);
	}
	
	public TargetFilter isOnBattlefield() {
		return (src, target) -> Cards.isOnZone(target, BattlefieldComponent.class);
	}
	
	public TargetFilter friendly() {
		return (src, target) -> Cards.getOwner(src) == Cards.getOwner(target);
	}
	
	public TargetFilter enemy() {
		return (src, target) -> Cards.getOwner(src) != Cards.getOwner(target);
	}

    public TargetFilter isPlayer() {
        return (src, target) -> target.hasComponent(PlayerComponent.class);
    }

    public TargetFilter isCreatureOnBattlefield() {
		return isCreature().and(isOnBattlefield());
	}
	
}
