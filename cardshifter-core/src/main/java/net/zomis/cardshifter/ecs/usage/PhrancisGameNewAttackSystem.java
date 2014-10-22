package net.zomis.cardshifter.ecs.usage;

import java.util.List;

import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

import com.cardshifter.modapi.actions.attack.AttackDamageAccumulating;
import com.cardshifter.modapi.actions.attack.AttackDamageHealAtEndOfTurn;
import com.cardshifter.modapi.actions.attack.AttackDamageYGO;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.ECSSystem;

public class PhrancisGameNewAttackSystem implements ECSMod {

	private final ECSMod parent = new PhrancisGame();
	
	@Override
	public void declareConfiguration(ECSGame game) {
		parent.declareConfiguration(game);
	}
	
	@Override
	public void setupGame(ECSGame game) {
		parent.setupGame(game);
		List<ECSSystem> attackSystems = game.findSystemsOfClass(AttackDamageYGO.class);
		game.removeSystem(attackSystems.get(0));
		game.addSystem(new AttackDamageAccumulating(PhrancisResources.ATTACK, PhrancisResources.HEALTH));
		game.addSystem(new AttackDamageHealAtEndOfTurn(PhrancisResources.HEALTH, PhrancisResources.MAX_HEALTH));
	}

}
