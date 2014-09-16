package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.zomis.cardshifter.ecs.base.Component;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;

import org.junit.Test;

public class ECSTest {

	private static class TestComponent extends Component {

		private final int value;
		
		public TestComponent(int value) {
			this.value = value;
		}
		
	}
	
	@Test
	public void entityWithHealth() {
		ECSGame game = new ECSGame();
		Entity entity = game.newEntity();
		entity.addComponent(new TestComponent(5));
		
		ComponentRetriever<TestComponent> retreiver = game.componentRetreiver(TestComponent.class);
		assertTrue(retreiver.has(entity));
		assertEquals(5, retreiver.get(entity).value);
	}
	
}
