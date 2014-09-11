package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSAction;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.components.ActionComponent;

import org.junit.Test;

public class ActionTest {

	private ECSGame game = new ECSGame();
	private Entity entity = game.newEntity().addComponent(new ActionComponent());
	private ComponentRetriever<ActionComponent> retriever = game.componentRetreiver(ActionComponent.class);
	private int used;
	
	@Test
	public void test() {
		ActionComponent actions = retriever.get(entity);
		assertEquals(Collections.emptySet(), actions.getActions());
		
		actions.addAction(new ECSAction(entity, "Use", action -> true, action -> this.used++));
		assertEquals(0, used);
		actions.getAction("Use").copy().perform();
		assertEquals(1, used);
	}
	
}
