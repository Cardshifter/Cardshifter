package com.cardshifter.modapi.base;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.actions.SpecificActionSystem;

public class RemoveSystemTest {
	
	private final AtomicInteger value = new AtomicInteger();
	private SpecificActionSystem system;
	
	@Test
	public void removeSystemTest() {
		ECSGame game = new ECSGame();
		ActionComponent act = new ActionComponent();
		Entity entity = game.newEntity().addComponent(act);
		act.addAction(new ECSAction(entity, "Test", e -> true, e -> {}));
		system = new SpecificActionSystem("Test") {
			@Override
			protected void onPerform(ActionPerformEvent event) {
				value.getAndIncrement();
				event.getEntity().getGame().removeSystem(system);
			}
		};
		game.addSystem(system);
		
		game.startGame();
		
		assertEquals(0, value.get());
		act.getAction("Test").perform(entity);
		assertEquals(1, value.get());
		act.getAction("Test").perform(entity);
		assertEquals(1, value.get());
		
		game.addSystem(system);
		act.getAction("Test").perform(entity);
		assertEquals(2, value.get());
	}

}
