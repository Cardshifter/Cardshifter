package net.zomis.cardshifter.ecs;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.base.Retriever;
import com.cardshifter.modapi.base.RetrieverSingleton;
import com.cardshifter.modapi.base.Retrievers;
import com.cardshifter.modapi.phase.Phase;
import com.cardshifter.modapi.phase.PhaseController;

public class InjectionTest {

	@RetrieverSingleton
	private PhaseController phases;
	
	@Retriever
	private ComponentRetriever<PlayerComponent> playerData;
	
	private int test;
	
	@Test
	public void injectOnAdd() {
		ECSGame game = new ECSGame();
		game.newEntity().addComponent(new PlayerComponent(21, "Test"));
		game.addSystem(new ECSSystem() {
			@RetrieverSingleton
			private PlayerComponent player;
			
			@Override
			public void startGame(ECSGame game) {
				assertEquals(21, player.getIndex());
				test++;
			}
		});
		game.startGame();
		assertEquals(1, test);
	}
	
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
