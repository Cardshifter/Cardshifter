package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.IEvent;

import org.junit.Test;

public class EventTest {

	private ECSGame game = new ECSGame();
	private List<TestEvent> pre = new ArrayList<>();
	private List<TestEvent> post = new ArrayList<>();
	private int value = 42;
	
	private static class TestEvent implements IEvent {
		public final String data;

		public TestEvent(String data) {
			this.data = data;
		}
	}
	
	@Test
	public void executeTestEvent() {
		assertEquals(42, value);
		game.getEvents().registerHandlerBefore(TestEvent.class, event -> {
			assertEquals(pre.size(), post.size());
			assertNotEquals(pre.size(), value);
			pre.add(event);
		});
		game.getEvents().registerHandlerAfter(TestEvent.class, event -> {
			post.add(event);
			assertEquals(pre.size(), post.size());
			assertEquals(pre.size(), value);
		});
		assertEquals(42, value);
		game.executeEvent(new TestEvent("Test1"), () -> value = pre.size());
		assertEquals(pre.size(), value);
		assertEquals(pre.size(), 1);
		assertEquals(pre, post);
		assertEquals("Test1", pre.get(0).data);
	}
	
}
