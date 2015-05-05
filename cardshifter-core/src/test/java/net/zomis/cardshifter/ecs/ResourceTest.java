package net.zomis.cardshifter.ecs;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.phase.Phase;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.phase.PhaseStartEvent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceData;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.ResourceValueChange;

public class ResourceTest {

	private enum TestResource implements ECSResource {
		TEST_A, TEST_B;
	}
	
	private ECSGame game;
	private Entity entity;
	private ComponentRetriever<ECSResourceMap> resMapper;
	private int resourceChange = 0;
	
	@Before
	public void setup() {
		game = new ECSGame();
		entity = game.newEntity();
		resMapper = game.componentRetreiver(ECSResourceMap.class);
		ECSResourceMap res = ECSResourceMap.createFor(entity);
		res.getResource(TestResource.TEST_A).set(10);
		res.getResource(TestResource.TEST_B).set(5);
		
		PhaseController controller = new PhaseController();
		controller.addPhase(new Phase(entity, "A"));
		controller.addPhase(new Phase(entity, "B"));
		entity.addComponent(controller);
	}
	
	@Test
	public void resourceEvents() {
		game.getEvents().registerHandlerAfter(this, ResourceValueChange.class, event -> this.resourceChange++);
		
		assertEquals(0, this.resourceChange);
		ECSResourceData resA = resMapper.get(entity).getResource(TestResource.TEST_A);
		resA.set(42);
		assertEquals(1, this.resourceChange);
		resA.set(100);
		assertEquals(2, this.resourceChange);
		resA.set(100);
		assertEquals(2, this.resourceChange); // no actual change, should not cause an event
	}

	@Test
	public void resourceGetsBonusFromOtherEntity() {
		ECSResourceData resA = resMapper.get(entity).getResource(TestResource.TEST_A);
		resA.set(5);
		
		Entity enchanter = game.newEntity();
		ECSResourceMap.createFor(enchanter);
		ECSResourceData bonusResource = resMapper.get(enchanter).getResource(TestResource.TEST_B);
		resA.setStrategy((ent, res, value) -> value + bonusResource.get());
		
		assertEquals(0, bonusResource.get());
		assertEquals(5, resA.get());
		bonusResource.set(10);
		assertEquals(15, resA.get());
	}
	
	@Test
	public void addResourceEachTurn() {
		game.addSystem(g -> g.getEvents().registerHandlerAfter(this, PhaseStartEvent.class, this::addResources));
		game.startGame();
		
		ECSResourceData resA = resMapper.get(entity).getResource(TestResource.TEST_A);
		ECSResourceData resB = resMapper.get(entity).getResource(TestResource.TEST_B);
		assertEquals("Resource A first turn", 10, resA.get());
		assertEquals("Resource B first turn", 5, resB.get());
		PhaseController controller = entity.getComponent(PhaseController.class);
		
		controller.nextPhase();
		assertEquals("Resource A turn 2", 10, resA.get());
		assertEquals("Resource B turn 2", 6, resB.get());
		controller.nextPhase();
		assertEquals("Resource A turn 3", 11, resA.get());
		assertEquals("Resource B turn 3", 6, resB.get());
		controller.nextPhase();
		assertEquals("Resource A turn 4", 11, resA.get());
		assertEquals("Resource B turn 4", 7, resB.get());
	}
	
	private void addResources(PhaseStartEvent event) {
		ECSResource resource = event.getNewPhase().getName().equals("A") ? TestResource.TEST_A : TestResource.TEST_B;
		entity.getComponent(ECSResourceMap.class).getResource(resource).change(1);
	}
	
}
