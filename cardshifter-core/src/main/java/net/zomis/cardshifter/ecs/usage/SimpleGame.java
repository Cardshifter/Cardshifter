package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.phase.PerformerMustBeCurrentPlayer;
import com.cardshifter.modapi.phase.Phase;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceDefault;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.GameOverIfNoHealth;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class SimpleGame implements ECSMod {
	private static final ECSResource HEALTH = new ECSResourceDefault("Health");
	
	@Override
	public void setupGame(ECSGame game) {
		PhaseController phaseController = new PhaseController();
		game.newEntity().addComponent(phaseController);
		
		for (int i = 1; i <= 2; i++) {
			PlayerComponent playerComponent = new PlayerComponent(i - 1, "Player" + i);
			Entity player = game.newEntity().addComponent(playerComponent);
			
			Phase playerPhase = new Phase(player, "Main");
			phaseController.addPhase(playerPhase);
			
			ActionComponent actions = new ActionComponent();
			player.addComponent(actions);
			
			ResourceRetriever health = ResourceRetriever.forResource(HEALTH);
			actions.addAction(new ECSAction(player, "End Turn", act -> phaseController.getCurrentPhase() == playerPhase, act -> phaseController.nextPhase()));
			actions.addAction(new ECSAction(player, "Damage me", act -> phaseController.getCurrentPhase() == playerPhase, act -> health.resFor(player).change(-2)));
			actions.addAction(new ECSAction(player, "Heal", act -> phaseController.getCurrentPhase() == playerPhase, act -> health.resFor(player).change(1)));
			
			ECSResourceMap.createFor(player).set(HEALTH, 10);
		}
		
		game.addSystem(new GameOverIfNoHealth(HEALTH));
		game.addSystem(new PerformerMustBeCurrentPlayer());
	}

}
