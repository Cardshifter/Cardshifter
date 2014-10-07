
package com.cardshifter.testmod.java;

import net.zomis.cardshifter.ecs.actions.ActionComponent;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.PlayerComponent;
import net.zomis.cardshifter.ecs.phase.Phase;
import net.zomis.cardshifter.ecs.phase.PhaseController;
import net.zomis.cardshifter.ecs.resources.ECSResourceMap;
import net.zomis.cardshifter.ecs.resources.ResourceRetriever;

/**
 *
 * @author Frank van Heeswijk
 */
public final class TestGame {
    private TestGame() {
        throw new UnsupportedOperationException();
    }
    
	public static ECSGame bareGame() {
		ECSGame game = new ECSGame();
		
		PhaseController phaseController = new PhaseController();
		game.newEntity().addComponent(phaseController);
		
		for (int i = 1; i <= 2; i++) {
			PlayerComponent playerComponent = new PlayerComponent(i - 1, "Player" + i);
			Entity player = game.newEntity().addComponent(playerComponent);
			
			Phase playerPhase = new Phase(player, "Main");
			phaseController.addPhase(playerPhase);
			
			ActionComponent actions = new ActionComponent();
			player.addComponent(actions);
			
			ResourceRetriever test = ResourceRetriever.forResource(TestResources.TEST);
			actions.addAction(new ECSAction(player, "End turn", act -> phaseController.getCurrentPhase() == playerPhase, act -> phaseController.nextPhase()));
			actions.addAction(new ECSAction(player, "Damage me", act -> phaseController.getCurrentPhase() == playerPhase, act -> test.resFor(player).change(-1)));
			actions.addAction(new ECSAction(player, "Heal", act -> phaseController.getCurrentPhase() == playerPhase, act -> test.resFor(player).change(1)));
			
			ECSResourceMap.createFor(player).set(TestResources.TEST, 10);
		}
		
		return game;
	}
}
