package net.zomis.cardshifter.ecs;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.phase.Phase;
import com.cardshifter.modapi.phase.PhaseController;

public class PhaseControllerTest {

	private ECSGame game;
	private PhaseController controller;
	private Entity owner;
	
	@Before
	public void before() {
		game = new ECSGame();
		controller = new PhaseController();
		owner = game.newEntity();
		owner.addComponent(controller);
	}
	
	@Test
	public void insertTemporaryAfterEnd() {
		controller.addPhase(new Phase(owner, "a"));
		controller.addPhase(new Phase(owner, "b"));
		assertTrue(controller.insertTemporaryPhaseAfter(new Phase(owner, "c"), phase -> phase.getName().equals("b")));
		
		assertEquals("a", controller.getCurrentPhase().getName());
		assertEquals("b", controller.nextPhase().getName());
		assertEquals("c", controller.nextPhase().getName());
		
		assertEquals("a", controller.nextPhase().getName());
		assertEquals("b", controller.nextPhase().getName());
		assertEquals("a", controller.nextPhase().getName());
	}
	
	@Test
	public void insertTemporaryAfter() {
		controller.addPhase(new Phase(owner, "a"));
		controller.addPhase(new Phase(owner, "b"));
		controller.addPhase(new Phase(owner, "c"));
		controller.addPhase(new Phase(owner, "e"));
		assertTrue(controller.insertTemporaryPhaseAfter(new Phase(owner, "d"), phase -> phase.getName().equals("c")));
		
		assertEquals("a", controller.getCurrentPhase().getName());
		assertEquals("b", controller.nextPhase().getName());
		assertEquals("c", controller.nextPhase().getName());
		assertEquals("d", controller.nextPhase().getName());
		assertEquals("e", controller.nextPhase().getName());
		
		assertEquals("a", controller.nextPhase().getName());
		assertEquals("b", controller.nextPhase().getName());
		assertEquals("c", controller.nextPhase().getName());
		assertEquals("e", controller.nextPhase().getName());
	}
	
	@Test
	public void insertTemporaryBefore() {
		controller.addPhase(new Phase(owner, "a"));
		controller.addPhase(new Phase(owner, "b"));
		controller.addPhase(new Phase(owner, "c"));
		controller.addPhase(new Phase(owner, "e"));
		assertTrue(controller.insertTemporaryPhaseBefore(new Phase(owner, "d"), phase -> phase.getName().equals("e")));
		
		assertEquals("a", controller.getCurrentPhase().getName());
		assertEquals("b", controller.nextPhase().getName());
		assertEquals("c", controller.nextPhase().getName());
		assertEquals("d", controller.nextPhase().getName());
		assertEquals("e", controller.nextPhase().getName());
		
		assertEquals("a", controller.nextPhase().getName());
		assertEquals("b", controller.nextPhase().getName());
		assertEquals("c", controller.nextPhase().getName());
		assertEquals("e", controller.nextPhase().getName());
	}
	
	@Test
	public void insertTemporary() {
		controller.addPhase(new Phase(owner, "a"));
		controller.addPhase(new Phase(owner, "b"));
		assertEquals("a", controller.getCurrentPhase().getName());
		assertEquals("b", controller.nextPhase().getName());
		controller.insertTemporaryPhaseNext(new Phase(owner, "c"));
		assertEquals("b", controller.getCurrentPhase().getName());
		assertEquals("c", controller.nextPhase().getName());
		assertEquals("a", controller.nextPhase().getName());
		assertEquals("b", controller.nextPhase().getName());
	}
	
	@Test
	public void addGetAdd() {
		controller.addPhase(new Phase(owner, "a"));
		assertEquals("a", controller.getCurrentPhase().getName());
		controller.addPhase(new Phase(owner, "b"));
		assertEquals("a", controller.getCurrentPhase().getName());
		controller.nextPhase();
		assertEquals("b", controller.getCurrentPhase().getName());
		controller.nextPhase();
		assertEquals("a", controller.getCurrentPhase().getName());
		controller.nextPhase();
		assertEquals("b", controller.getCurrentPhase().getName());
	}
	
	@Test
	public void autoAdd() {
		controller.addPhase(new Phase(owner, "a"));
		controller.addPhase(new Phase(owner, "b"));
		assertEquals("a", controller.getCurrentPhase().getName());
		controller.nextPhase();
		assertEquals("b", controller.getCurrentPhase().getName());
		controller.nextPhase();
		assertEquals("a", controller.getCurrentPhase().getName());
		controller.nextPhase();
		assertEquals("b", controller.getCurrentPhase().getName());
	}
	
}
