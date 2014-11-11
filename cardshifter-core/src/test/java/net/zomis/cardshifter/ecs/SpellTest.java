package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.zomis.cardshifter.ecs.effects.Effects;
import net.zomis.cardshifter.ecs.effects.FilterComponent;
import net.zomis.cardshifter.ecs.effects.Filters;
import net.zomis.cardshifter.ecs.usage.EntityCannotUseSystem;
import net.zomis.cardshifter.ecs.usage.OpponentCannotUseSystem;
import net.zomis.cardshifter.ecs.usage.PhrancisEffects;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;
import net.zomis.cardshifter.ecs.usage.UntilEndOfOwnerTurnSystem;

import org.junit.Test;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.GameTest;
import com.cardshifter.modapi.players.Players;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class SpellTest extends GameTest {
	
	private static final String NO_NAME = "No name";

	private PhrancisGame mod;
	
	private ResourceRetriever scrap = ResourceRetriever.forResource(PhrancisResources.SCRAP);

	@Test
	public void preventOpponentAction() {
		assertNotNull(currentPlayer());
		Effects eff = new Effects();
		Entity spell = mod.createSpell(NO_NAME, hand.get(currentPlayer()), 0, 0, eff.addSystem(e -> new OpponentCannotUseSystem(Players.findOwnerFor(e), PhrancisGame.END_TURN_ACTION)));
		useAction(spell, PhrancisGame.USE_ACTION);
		assertTrue(spell.isRemoved());
		nextPhase();
		useFail(currentPlayer(), PhrancisGame.END_TURN_ACTION);
	}
	
	@Test
	public void frezeOpponentMinion() {
		assertNotNull(currentPlayer());
		Effects eff = new Effects();
		Entity freezeTarget = mod.createCreature(0, field.get(getOpponent()), 1, 1, "B0T", 0);
		Entity spell = mod.createTargetSpell(NO_NAME, hand.get(currentPlayer()), 0, 0, eff.giveTarget(e -> new UntilEndOfOwnerTurnSystem(e, new EntityCannotUseSystem(e, PhrancisGame.ATTACK_ACTION))),
			new FilterComponent(new Filters().isCreatureOnBattlefield()));
		useActionWithTarget(spell, PhrancisGame.USE_ACTION, freezeTarget);
		assertTrue(spell.isRemoved());
		nextPhase();
		useFail(freezeTarget, PhrancisGame.ATTACK_ACTION);
		nextPhase();
		nextPhase();
		useActionWithTarget(freezeTarget, PhrancisGame.ATTACK_ACTION, getOpponent());
	}
	
	@Test
	public void scrapAll() {
		assertNotNull(currentPlayer());
		PhrancisEffects eff = new PhrancisEffects();
		scrap.resFor(currentPlayer()).set(2);
		scrap.resFor(getOpponent()).set(3);
		assertResource(currentPlayer(), PhrancisResources.SCRAP, 2);
		assertResource(getOpponent(), PhrancisResources.SCRAP, 3);
		
		mod.createCreature(0, field.get(currentPlayer()), 0, 1, "Temp", 4);
		mod.createCreature(0, field.get(getOpponent()), 0, 1, "Temp", 2);
		mod.createCreature(0, field.get(getOpponent()), 0, 1, "Temp", 1);
		Entity spell = mod.createSpell(NO_NAME, hand.get(currentPlayer()), 0, 0, eff.scrapAll());
		useAction(spell, PhrancisGame.USE_ACTION);
		assertTrue(spell.isRemoved());
		
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
