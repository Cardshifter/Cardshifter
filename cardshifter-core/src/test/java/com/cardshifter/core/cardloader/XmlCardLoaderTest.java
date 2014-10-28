
package com.cardshifter.core.cardloader;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static org.junit.Assert.*;
import org.junit.Test;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.NameComponent;
import com.cardshifter.modapi.cards.CardImageComponent;
import com.cardshifter.modapi.cards.CardTypeComponent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;

/**
 *
 * @author Frank van Heeswijk
 */
public class XmlCardLoaderTest {
	private static enum TestResources implements ECSResource {
		TR1, TR2;
	}
	
	@Test
	public void testLoadNoCards() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("no-cards.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, new ECSResource[0]);
		
		assertEquals(0, entities.size());
	}
	
	@Test
	public void testLoadOneCard() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values());
		
		Entity card = findEntityWithName(entities, "Test 1");
		assertEquals("Test 1", card.getComponent(NameComponent.class).getName());
		assertEquals("test.jpg", card.getComponent(CardImageComponent.class).getCardImage());
		assertEquals("testcard", card.getComponent(CardTypeComponent.class).getCardType());
		assertEquals(5, card.getComponent(ECSResourceMap.class).getResource(TestResources.TR1).get());
		assertEquals(-6, card.getComponent(ECSResourceMap.class).getResource(TestResources.TR2).get());
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
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, new ECSResource[] { tr1, tr2 });
		
		
		Entity card = findEntityWithName(entities, "Test 1");
		assertEquals("Test 1", card.getComponent(NameComponent.class).getName());
		assertEquals("test.jpg", card.getComponent(CardImageComponent.class).getCardImage());
		assertEquals("testcard", card.getComponent(CardTypeComponent.class).getCardType());
		assertEquals(5, card.getComponent(ECSResourceMap.class).getResource(tr1).get());
		assertEquals(-6, card.getComponent(ECSResourceMap.class).getResource(tr2).get());
	}
	
	@Test
	public void testLoadOneCardNoResources() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card-no-resources.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values());
		
		Entity card = findEntityWithName(entities, "Test 1");
		assertEquals("Test 1", card.getComponent(NameComponent.class).getName());
		assertEquals("test.jpg", card.getComponent(CardImageComponent.class).getCardImage());
		assertEquals("testcard", card.getComponent(CardTypeComponent.class).getCardType());
	}
	
	@Test
	public void testLoadOneCardWithSpecialCharacters() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card-with-special-characters.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values());
		
		Entity card = findEntityWithName(entities, "Test #1 Test @2 3. Test's");
		assertEquals("Test #1 Test @2 3. Test's", card.getComponent(NameComponent.class).getName());
		assertEquals("test.jpg", card.getComponent(CardImageComponent.class).getCardImage());
		assertEquals("testcard", card.getComponent(CardTypeComponent.class).getCardType());	
		assertEquals(5, card.getComponent(ECSResourceMap.class).getResource(TestResources.TR1).get());
		assertEquals(-6, card.getComponent(ECSResourceMap.class).getResource(TestResources.TR2).get());
	}
	
	@Test
	public void testLoadOneCardWithMissingImage() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card-with-missing-image.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values());
		
		Entity card = findEntityWithName(entities, "Test 1");
		assertEquals("Test 1", card.getComponent(NameComponent.class).getName());
		assertEquals(5, card.getComponent(ECSResourceMap.class).getResource(TestResources.TR1).get());
		assertEquals(-6, card.getComponent(ECSResourceMap.class).getResource(TestResources.TR2).get());
	}
	
	@Test
	public void testLoadTwoCards() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("two-cards.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		Collection<Entity> entities = xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values());
		
		Entity card1 = findEntityWithName(entities, "Test 1");
		assertEquals("Test 1", card1.getComponent(NameComponent.class).getName());
		assertEquals("test1.jpg", card1.getComponent(CardImageComponent.class).getCardImage());
		assertEquals("testcard", card1.getComponent(CardTypeComponent.class).getCardType());
		assertEquals(5, card1.getComponent(ECSResourceMap.class).getResource(TestResources.TR1).get());
		assertEquals(-6, card1.getComponent(ECSResourceMap.class).getResource(TestResources.TR2).get());
		
		Entity card2 = findEntityWithName(entities, "Test 2");
		assertEquals("Test 2", card2.getComponent(NameComponent.class).getName());
		assertEquals("test2.jpg", card2.getComponent(CardImageComponent.class).getCardImage());
		assertEquals("testcard2", card2.getComponent(CardTypeComponent.class).getCardType());
		assertEquals(3, card2.getComponent(ECSResourceMap.class).getResource(TestResources.TR1).get());
		assertEquals(-8, card2.getComponent(ECSResourceMap.class).getResource(TestResources.TR2).get());
	}
	
	
	@Test(expected = CardLoadingException.class)
	public void testLoadOneCardWithDoubleResource() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card-with-double-resource.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values());
	}
	
	@Test(expected = CardLoadingException.class)
	public void testLoadOneCardResourceNotFound() throws URISyntaxException, CardLoadingException {
		Path xmlFile = Paths.get(getClass().getResource("one-card-resource-not-found.xml").toURI());
		
		ECSGame game = new ECSGame();
		
		XmlCardLoader xmlCardLoader = new XmlCardLoader();
		xmlCardLoader.loadCards(xmlFile, game::newEntity, TestResources.values());
	}
	
	private Entity findEntityWithName(final Collection<Entity> entities, final String name) {
		return entities.stream()
			.filter(entity -> entity.getComponent(NameComponent.class).getName().equals(name))
			.findFirst()
			.get();
	}
}