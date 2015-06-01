package com.cardshifter.modapi.actions.attack;

import com.cardshifter.modapi.actions.TargetableCheckEvent;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.players.Players;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

import java.util.Objects;
import java.util.function.Predicate;

public class AttackTargetMinionsFirstThenPlayer extends SpecificActionTargetSystem {

	private final ResourceRetriever attackFirstResource;
    private final Predicate<Entity> shouldAttackFirst;

	public AttackTargetMinionsFirstThenPlayer(ECSResource requiredResource) {
		super("Attack");
		this.attackFirstResource = ResourceRetriever.forResource(requiredResource);
        this.shouldAttackFirst = e -> attackFirstResource.getOrDefault(e, 0) > 0;
	}

    private boolean hasTauntMinions(Entity player) {
        BattlefieldComponent battlefieldComponent = player.getComponent(BattlefieldComponent.class);
        Objects.requireNonNull(battlefieldComponent, "Entity does not have a battlefield: " + player.debug());
        return battlefieldComponent.stream().anyMatch(shouldAttackFirst);
    }
	
	protected void checkTargetable(TargetableCheckEvent event) {
		Entity target = event.getTarget();
		if (target.hasComponent(CardComponent.class)) {
			if (Cards.isOwnedByCurrentPlayer(target)) {
				event.setAllowed(false);
			}
			if (!Cards.isOnZone(target, BattlefieldComponent.class)) {
				event.setAllowed(false);
                return;
			}
            if (!shouldAttackFirst.test(target) && hasTauntMinions(Players.findOwnerFor(target))) {
                event.setAllowed(false);
            }
		}
		else if (target.hasComponent(PlayerComponent.class)) {
			if (target == ComponentRetriever.singleton(target.getGame(), PhaseController.class).getCurrentEntity()) {
				event.setAllowed(false);
			}
            if (hasTauntMinions(target)) {
                event.setAllowed(false);
            }
		}
		else {
			event.setAllowed(false);
		}
	}

}
