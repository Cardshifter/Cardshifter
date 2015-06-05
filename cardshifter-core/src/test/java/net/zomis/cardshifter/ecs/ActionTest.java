package net.zomis.cardshifter.ecs;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.cardshifter.modapi.actions.*;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;

import java.util.Collections;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ActionTest {

	private ECSGame game;
	private Entity entity;
	private ComponentRetriever<ActionComponent> retriever = ComponentRetriever.retreiverFor(ActionComponent.class);

	@Before
	public void before() {
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
		String name = "Use";
		game.addSystem(new SpecificActionSystem(name) {
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

		@SuppressWarnings("unchecked")
		Consumer<ECSAction> perform = (Consumer<ECSAction>) mock(Consumer.class);

		actions.addAction(new ECSAction(entity, name, action -> true, perform));
		actions.getAction(name).copy().perform(entity);
		verifyNoMoreInteractions(perform);

		assertFalse(actions.getAction(name).isAllowed(entity));
	}

}
