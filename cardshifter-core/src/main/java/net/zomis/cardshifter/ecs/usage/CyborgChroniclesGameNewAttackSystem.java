package net.zomis.cardshifter.ecs.usage;

import java.util.List;
import java.util.function.BiPredicate;

import com.cardshifter.modapi.actions.attack.AttackDamageAccumulating;
import com.cardshifter.modapi.actions.attack.AttackDamageHealAtEndOfTurn;
import com.cardshifter.modapi.actions.attack.AttackDamageYGO;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class CyborgChroniclesGameNewAttackSystem extends CyborgChroniclesGame {

	@Override
	public void setupGame(ECSGame game) {
		super.setupGame(game);
		List<AttackDamageYGO> attackSystems = game.findSystemsOfClass(AttackDamageYGO.class);
		game.removeSystem(attackSystems.get(0));

		ResourceRetriever allowCounterAttackRes = ResourceRetriever.forResource(CyborgChroniclesResources.DENY_COUNTERATTACK);
		BiPredicate<Entity, Entity> allowCounterAttack =
				(attacker, defender) -> allowCounterAttackRes.getOrDefault(attacker, 0) == 0;

		game.addSystem(new AttackDamageAccumulating(CyborgChroniclesResources.ATTACK, CyborgChroniclesResources.HEALTH,
				allowCounterAttack));
		game.addSystem(new AttackDamageHealAtEndOfTurn(CyborgChroniclesResources.HEALTH, CyborgChroniclesResources.MAX_HEALTH));
	}

}
