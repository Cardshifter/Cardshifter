package net.zomis.cardshifter.ecs;

import static org.junit.Assert.*;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.components.HealthComponent;

import org.junit.Test;

public class ECSTest {

	@Test
	public void entityWithHealth() {
		ECSGame game = new ECSGame();
		Entity entity = game.newEntity();
		entity.addComponent(new HealthComponent(5));
		
		ComponentRetriever<HealthComponent> retreiver = game.componentRetreiver(HealthComponent.class);
		assertTrue(retreiver.has(entity));
		assertEquals(5, retreiver.get(entity).getHealth());
	}
	
}
