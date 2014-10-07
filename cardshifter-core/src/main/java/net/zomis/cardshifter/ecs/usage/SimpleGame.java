package net.zomis.cardshifter.ecs.usage;

import net.zomis.cardshifter.ecs.actions.ActionComponent;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.ECSMod;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.components.PlayerComponent;
import net.zomis.cardshifter.ecs.phase.Phase;
import net.zomis.cardshifter.ecs.phase.PhaseController;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ECSResourceDefault;
import net.zomis.cardshifter.ecs.resources.ECSResourceMap;
import net.zomis.cardshifter.ecs.resources.ResourceRetriever;
import net.zomis.cardshifter.ecs.systems.GameOverIfNoHealth;

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
			actions.addAction(new ECSAction(player, "Damage me", act -> phaseController.getCurrentPhase() == playerPhase, act -> health.resFor(player).change(-1)));
			actions.addAction(new ECSAction(player, "Heal", act -> phaseController.getCurrentPhase() == playerPhase, act -> health.resFor(player).change(1)));
			
			ECSResourceMap.createFor(player).set(HEALTH, 10);
		}
		
		game.addSystem(new GameOverIfNoHealth(HEALTH));
		game.addSystem(new PerformerMustBeCurrentPlayer());
	}

}
