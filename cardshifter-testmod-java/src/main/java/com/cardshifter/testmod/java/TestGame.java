
package com.cardshifter.testmod.java;

import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.phase.Phase;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.ResourceRetriever;

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
