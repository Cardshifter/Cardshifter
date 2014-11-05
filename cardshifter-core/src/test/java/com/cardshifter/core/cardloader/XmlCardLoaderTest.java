
package com.cardshifter.core.cardloader;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.junit.Assert.*;
import org.junit.Test;

import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.IdComponent;
import com.cardshifter.modapi.resources.ECSResource;

/**
 *
 * @author Frank van Heeswijk
 */
public class XmlCardLoaderTest {
	private static enum NameAttributes implements ECSAttribute {
		NAME;
	}
	
	private static enum TestResources implements ECSResource {
		TR1, TR2;
	}
	
	private static enum TestAttributes implements ECSAttribute {
		NAME, IMAGE, CARDTYPE; 
	}
	
	private static enum HealthResources implements ECSResource {
		MAX_HEALTH;
	}
	
	private static enum DuplicateHealthResources implements ECSResource {
		MAX_HEALTH, MAXHEALTH, maxhealth;
	}
	
	@Test
	public void testLoadNoCards() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("no-cards.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, new ECSResource[0], new ECSAttribute[0]);
		
		assertEquals(0, entities.size());
	}
	
	@Test
	public void testLoadOneCard() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values(), TestAttributes.values());
		
		Entity card = findEntityWithId(entities, "1");
		assertEquals("1", card.getComponent(IdComponent.class).getId());
		assertEquals("Test 1", TestAttributes.NAME.getFor(card));
		assertEquals("test.jpg", TestAttributes.IMAGE.getFor(card));
		assertEquals("testcard", TestAttributes.CARDTYPE.getFor(card));
		assertEquals(5, TestResources.TR1.getFor(card));
		assertEquals(-6, TestResources.TR2.getFor(card));
	}
	
	@Test
	public void testLoadOneCardWithoutUsingECSResource() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		ECSResource tr1 = new ECSResource() {
			@Override
			public String toString() {
				return "TR1";
			}
		};
		ECSResource tr2 = new ECSResource() {
			@Override
			public String toString() {
				return "TR2";
			}
		};
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, new ECSResource[] { tr1, tr2 }, TestAttributes.values());
		
		Entity card = findEntityWithId(entities, "1");
		assertEquals("1", card.getComponent(IdComponent.class).getId());
		assertEquals("Test 1", TestAttributes.NAME.getFor(card));
		assertEquals("test.jpg", TestAttributes.IMAGE.getFor(card));
		assertEquals("testcard", TestAttributes.CARDTYPE.getFor(card));
		assertEquals(5, tr1.getFor(card));
		assertEquals(-6, tr2.getFor(card));
	}
	
	@Test
	public void testLoadOneCardNoResources() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card-no-resources.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values(), TestAttributes.values());
		
		Entity card = findEntityWithId(entities, "1");
		assertEquals("1", card.getComponent(IdComponent.class).getId());
		assertEquals("Test 1", TestAttributes.NAME.getFor(card));
		assertEquals("test.jpg", TestAttributes.IMAGE.getFor(card));
		assertEquals("testcard", TestAttributes.CARDTYPE.getFor(card));
	}
	
	@Test
	public void testLoadOneCardWithSpecialCharacters() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card-with-special-characters.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values(), TestAttributes.values());
		
		Entity card = findEntityWithId(entities, "1");
		assertEquals("1", card.getComponent(IdComponent.class).getId());
		assertEquals("Test #1 Test @2 3. Test's", TestAttributes.NAME.getFor(card));
		assertEquals("test.jpg", TestAttributes.IMAGE.getFor(card));
		assertEquals("testcard", TestAttributes.CARDTYPE.getFor(card));
		assertEquals(5, TestResources.TR1.getFor(card));
		assertEquals(-6, TestResources.TR2.getFor(card));
	}
	
	@Test
	public void testLoadTwoCards() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("two-cards.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values(), TestAttributes.values());
		
		Entity card = findEntityWithId(entities, "1");
		assertEquals("1", card.getComponent(IdComponent.class).getId());
		assertEquals("Test 1", TestAttributes.NAME.getFor(card));
		assertEquals("test1.jpg", TestAttributes.IMAGE.getFor(card));
		assertEquals("testcard", TestAttributes.CARDTYPE.getFor(card));
		assertEquals(5, TestResources.TR1.getFor(card));
		assertEquals(-6, TestResources.TR2.getFor(card));
		
		Entity card2 = findEntityWithId(entities, "2");
		assertEquals("2", card2.getComponent(IdComponent.class).getId());
		assertEquals("Test 2", TestAttributes.NAME.getFor(card2));
		assertEquals("test2.jpg", TestAttributes.IMAGE.getFor(card2));
		assertEquals("testcard2", TestAttributes.CARDTYPE.getFor(card2));
		assertEquals(3, TestResources.TR1.getFor(card2));
		assertEquals(-8, TestResources.TR2.getFor(card2));
	}
	
	@Test
	public void testLoadFourCardsSanitized() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("four-cards-sanitized.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, HealthResources.values(), NameAttributes.values());
		
		Entity card1 = findEntityWithId(entities, "1");
		assertEquals("Test 1", NameAttributes.NAME.getFor(card1));
		assertEquals(10, HealthResources.MAX_HEALTH.getFor(card1));
		
		Entity card2 = findEntityWithId(entities, "2");
		assertEquals("Test 2", NameAttributes.NAME.getFor(card2));
		assertEquals(10, HealthResources.MAX_HEALTH.getFor(card2));
		
		Entity card3 = findEntityWithId(entities, "3");
		assertEquals("Test 3", NameAttributes.NAME.getFor(card3));
		assertEquals(10, HealthResources.MAX_HEALTH.getFor(card3));
		
		Entity card4 = findEntityWithId(entities, "4");
		assertEquals("Test 4", NameAttributes.NAME.getFor(card4));
		assertEquals(10, HealthResources.MAX_HEALTH.getFor(card4));
	}
	
	@Test(expected = CardLoadingException.class)
	public void testLoadFourCardsSanitizedIncorrectHealthResourcesMapping() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("four-cards-sanitized.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		xmlCardLoader.loadCards(xmlFile, game::newEntity, DuplicateHealthResources.values(), NameAttributes.values());
	}
	
	@Test(expected = CardLoadingException.class)
	public void testLoadOneCardWithDoubleResource() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card-with-double-resource.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values(), TestAttributes.values());
	}
	
	@Test(expected = CardLoadingException.class)
	public void testLoadOneCardResourceNotFound() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card-resource-not-found.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values(), TestAttributes.values());
	}
	
	@Test(expected = CardLoadingException.class)
	public void testLoadOneCardWithDoubleSanitizedResources() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card-with-double-sanitized-resource.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		xmlCardLoader.loadCards(xmlFile, game::newEntity, HealthResources.values(), NameAttributes.values());
	}
	
	@Test(expected = CardLoadingException.class)
	public void testLoadTwoCardsWithDuplicateIds() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("two-cards-with-duplicate-ids.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		xmlCardLoader.loadCards(xmlFile, game::newEntity, null, null);
	}
	
	private Entity findEntityWithId(final Collection<Entity> entities, final String id) {
		return entities.stream()
			.filter(entity -> entity.getComponent(IdComponent.class).getId().equals(id))
			.findFirst()
			.get();
	}
}