package com.cardshifter.modapi.actions.enchant;

import java.util.*;

import com.cardshifter.modapi.actions.TargetableCheckEvent;
import com.cardshifter.modapi.actions.attack.SpecificActionTargetSystem;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.Cards;

public class EnchantTargetCreatureTypes extends SpecificActionTargetSystem {

	private final String[] allowedCreatureTypes;
	private final ComponentRetriever<CreatureTypeComponent> type = ComponentRetriever.retreiverFor(CreatureTypeComponent.class);

	public EnchantTargetCreatureTypes(String... allowedCreatureTypes) {
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

        CreatureTypeComponent types = type.get(event.getTarget());
		if (types.noneMatch(allowedCreatureTypes)) {
			event.setAllowed(false);
		}
	}

}
