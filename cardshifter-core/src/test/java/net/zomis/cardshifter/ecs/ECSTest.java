package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;

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
