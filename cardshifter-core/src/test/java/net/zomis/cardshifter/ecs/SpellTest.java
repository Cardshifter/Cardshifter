package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertNotNull;
import net.zomis.cardshifter.ecs.usage.Effects;
import net.zomis.cardshifter.ecs.usage.OpponentCannotUseSystem;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

import org.junit.Test;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.GameTest;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class SpellTest extends GameTest {
	
	private PhrancisGame mod;
	
	private ResourceRetriever scrap = ResourceRetriever.forResource(PhrancisResources.SCRAP);

	@Test
	public void preventOpponentAction() {
		assertNotNull(currentPlayer());
		Effects eff = new Effects();
		Entity spell = mod.createSpell(hand.get(currentPlayer()), 0, 0, eff.addSystem(e -> new OpponentCannotUseSystem(e, PhrancisGame.END_TURN_ACTION)));
		useAction(spell, PhrancisGame.USE_ACTION);
		nextPhase();
		useFail(currentPlayer(), PhrancisGame.END_TURN_ACTION);
	}
	
	@Test
	public void scrapAll() {
		assertNotNull(currentPlayer());
		Effects eff = new Effects();
		scrap.resFor(currentPlayer()).set(2);
		scrap.resFor(getOpponent()).set(3);
		assertResource(currentPlayer(), PhrancisResources.SCRAP, 2);
		assertResource(getOpponent(), PhrancisResources.SCRAP, 3);
		
		mod.createCreature(0, field.get(currentPlayer()), 0, 1, "Temp", 4);
		mod.createCreature(0, field.get(getOpponent()), 0, 1, "Temp", 2);
		mod.createCreature(0, field.get(getOpponent()), 0, 1, "Temp", 1);
		Entity spell = mod.createSpell(hand.get(currentPlayer()), 0, 0, eff.scrapAll());
		useAction(spell, PhrancisGame.USE_ACTION);
		
		assertResource(currentPlayer(), PhrancisResources.SCRAP, 2 + 4);
		assertResource(getOpponent(), PhrancisResources.SCRAP, 3 + 3);
	}

	@Override
	protected void setupGame(ECSGame game) {
		mod = new PhrancisGame();
		mod.declareConfiguration(game);
		mod.setupGame(game);
	}

	@Override
	protected void onAfterGameStart() {
		phase.nextPhase();
	}

}
