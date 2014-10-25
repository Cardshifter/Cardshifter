package net.zomis.cardshifter.ecs.usage;

import java.util.List;

import com.cardshifter.modapi.actions.attack.AttackDamageAccumulating;
import com.cardshifter.modapi.actions.attack.AttackDamageHealAtEndOfTurn;
import com.cardshifter.modapi.actions.attack.AttackDamageYGO;
import com.cardshifter.modapi.base.ECSGame;

public class PhrancisGameNewAttackSystem extends PhrancisGame {

	@Override
	public void setupGame(ECSGame game) {
		super.setupGame(game);
		List<AttackDamageYGO> attackSystems = game.findSystemsOfClass(AttackDamageYGO.class);
		game.removeSystem(attackSystems.get(0));
		game.addSystem(new AttackDamageAccumulating(PhrancisResources.ATTACK, PhrancisResources.HEALTH));
		game.addSystem(new AttackDamageHealAtEndOfTurn(PhrancisResources.HEALTH, PhrancisResources.MAX_HEALTH));
	}

}
