package net.zomis.cardshifter.ecs.usage;

import java.util.Arrays;

import net.zomis.cardshifter.ecs.actions.TargetableCheckEvent;
import net.zomis.cardshifter.ecs.actions.attack.SpecificActionTargetSystem;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.cards.BattlefieldComponent;
import net.zomis.cardshifter.ecs.cards.CardComponent;
import net.zomis.cardshifter.ecs.cards.Cards;
import net.zomis.cardshifter.ecs.components.CreatureTypeComponent;

public class EnchantTargetCreatureTypes extends SpecificActionTargetSystem {

	private final String[] allowedCreatureTypes;
	private final ComponentRetriever<CreatureTypeComponent> type = ComponentRetriever.retreiverFor(CreatureTypeComponent.class);

	public EnchantTargetCreatureTypes(String[] allowedCreatureTypes) {
		super("Enchant");
		this.allowedCreatureTypes = Arrays.copyOf(allowedCreatureTypes, allowedCreatureTypes.length);
	}

	@Override
	protected void checkTargetable(TargetableCheckEvent event) {
		if (!event.getTarget().hasComponent(CardComponent.class)) {
			event.setAllowed(false);
			return;
		}
		if (!Cards.isOnZone(event.getTarget(), BattlefieldComponent.class)) {
			event.setAllowed(false);
		}
		if (!Cards.isOwnedByCurrentPlayer(event.getTarget())) {
			event.setAllowed(false);
		}
		
		if (!type.has(event.getTarget())) {
			event.setAllowed(false);
			return;
		}
		String creatureType = type.get(event.getTarget()).getCreatureType();
		if (Arrays.stream(allowedCreatureTypes).noneMatch(str -> str.equals(creatureType))) {
			event.setAllowed(false);
		}
	}

}
