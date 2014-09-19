package net.zomis.cardshifter.ecs;

import static org.junit.Assert.*;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.Retriever;
import net.zomis.cardshifter.ecs.base.RetrieverSingleton;
import net.zomis.cardshifter.ecs.base.Retrievers;
import net.zomis.cardshifter.ecs.components.PlayerComponent;
import net.zomis.cardshifter.ecs.phase.Phase;
import net.zomis.cardshifter.ecs.phase.PhaseController;

import org.junit.Test;

public class InjectionTest {

	@RetrieverSingleton
	private PhaseController phases;
	
	@Retriever
	private ComponentRetriever<PlayerComponent> playerData;
	
	@Test
	public void inject() {
		ECSGame game = new ECSGame();
		PhaseController phase = new PhaseController();
		phase.addPhase(new Phase(null, "Main"));
		game.newEntity().addComponent(phase);
		Entity player = game.newEntity().addComponent(new PlayerComponent(42, "Tester"));
		
		Retrievers.inject(this, game);
		
		assertEquals(42, playerData.get(player).getIndex());
		assertEquals(phase, phases);
		assertEquals("Main", phases.getCurrentPhase().getName());
	}
	
}
