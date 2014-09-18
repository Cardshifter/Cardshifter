package net.zomis.cardshifter.ecs;

import static org.junit.Assert.*;

import java.util.Collections;

import net.zomis.cardshifter.ecs.actions.ActionAllowedCheckEvent;
import net.zomis.cardshifter.ecs.actions.ActionComponent;
import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.actions.SpecificActionSystem;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;

import org.junit.Before;
import org.junit.Test;

public class ActionTest {

	private ECSGame game;
	private Entity entity;
	private ComponentRetriever<ActionComponent> retriever = ComponentRetriever.retreiverFor(ActionComponent.class);
	private int used;
	
	@Before
	public void das() {
		game = new ECSGame();
		entity = game.newEntity().addComponent(new ActionComponent());
	}
	
	@Test
	public void test() {
		game.startGame();
		ActionComponent actions = retriever.get(entity);
		assertEquals(Collections.emptySet(), actions.getActions());
		
		actions.addAction(new ECSAction(entity, "Use", action -> true, action -> this.used++));
		assertEquals(0, used);
		actions.getAction("Use").copy().perform(entity);
		assertEquals(1, used);
	}
	
	@Test
	public void deniedActionWithSystem() {
		game.addSystem(new SpecificActionSystem("Use") {
			@Override
			protected void isAllowed(ActionAllowedCheckEvent event) {
				event.setAllowed(false);
			}
			@Override
			protected void onPerform(ActionPerformEvent event) { }
		});
		game.startGame();
		
		ActionComponent actions = retriever.get(entity);
		assertEquals(Collections.emptySet(), actions.getActions());
		
		actions.addAction(new ECSAction(entity, "Use", action -> true, action -> this.used++));
		assertEquals(0, used);
		assertFalse(actions.getAction("Use").isAllowed(entity));
		assertEquals(0, used);
	}
	
}
