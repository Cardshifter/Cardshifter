package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertNotNull;
import net.zomis.cardshifter.ecs.usage.Effects;
import net.zomis.cardshifter.ecs.usage.OpponentCannotUseSystem;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;

import org.junit.Test;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;

public class SpellTest extends GameTest {
	
	private PhrancisGame mod;

	@Test
	public void preventOpponentAction() throws Exception {
		phase.nextPhase();
		assertNotNull(currentPlayer());
		Effects eff = new Effects();
		Entity spell = mod.createSpell(hand.get(currentPlayer()), 0, 0, eff.system(e -> new OpponentCannotUseSystem(e, PhrancisGame.END_TURN_ACTION)));
		useAction(spell, PhrancisGame.USE_ACTION);
		nextPhase();
		useFail(currentPlayer(), PhrancisGame.END_TURN_ACTION);
	}

	@Override
	protected void setupGame(ECSGame game) {
		mod = new PhrancisGame();
		mod.declareConfiguration(game);
		mod.setupGame(game);
//		game.findSystemsOfClass(MulliganSingleCards.class).forEach(game::removeSystem);
	}

	@Override
	protected void onBefore() {
	}

	

}
