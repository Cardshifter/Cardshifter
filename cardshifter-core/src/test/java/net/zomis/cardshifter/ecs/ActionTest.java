package net.zomis.cardshifter.ecs;

import net.zomis.cardshifter.ecs.actions.*;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
	public void testPerformAllowedAction() {
		game.startGame();
		ActionComponent actions = retriever.get(entity);
		assertEquals(Collections.<String>emptySet(), actions.getActions());

		@SuppressWarnings("unchecked")
		Consumer<ECSAction> perform = (Consumer<ECSAction>) mock(Consumer.class);

		String name = "Use";
		actions.addAction(new ECSAction(entity, name, action -> true, perform));
		actions.getAction(name).copy().perform(entity);
		verify(perform).accept(Mockito.any(ECSAction.class));
	}

	@Test
	public void deniedActionWithSystem() {
		game.addSystem(new SpecificActionSystem("Use") {
			@Override
			protected void isAllowed(ActionAllowedCheckEvent event) {
				event.setAllowed(false);
			}

			@Override
			protected void onPerform(ActionPerformEvent event) {
			}
		});
		game.startGame();

		ActionComponent actions = retriever.get(entity);
		assertEquals(Collections.<String>emptySet(), actions.getActions());

		actions.addAction(new ECSAction(entity, "Use", action -> true, action -> this.used++));
		assertEquals(0, used);
		assertFalse(actions.getAction("Use").isAllowed(entity));
		assertEquals(0, used);
	}

}
