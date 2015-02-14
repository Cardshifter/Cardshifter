
package com.cardshifter.core.cardloader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import static com.cardshifter.core.cardloader.CardLoaderHelper.*;
import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.IdComponent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * A card loader to load cards from XML files.
 *
 * @author Frank van Heeswijk
 */
public class XmlCardLoader implements CardLoader<Path> {
	@Override
	public Collection<Entity> loadCards(final Path path, final ECSGame game, final ECSMod mod, final ECSResource[] resources, final ECSAttribute[] attributes) throws CardLoadingException {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(game, "game");
		Objects.requireNonNull(mod, "mod");

		List<ECSResource> resourcesList = (resources == null) ? Arrays.asList() : Arrays.asList(resources);
		List<ECSAttribute> attributesList = (attributes == null) ? Arrays.asList() : Arrays.asList(attributes);
		
		try {
			SAXBuilder saxBuilder = new SAXBuilder();
			Document document = saxBuilder.build(path.toFile());
			
			XMLOutputter xmlOutputter = new XMLOutputter(Format.getCompactFormat().setExpandEmptyElements(true));
			String unformattedXmlString = xmlOutputter.outputString(document);
			
			JacksonXmlModule xmlModule = new JacksonXmlModule();
			xmlModule.setDefaultUseWrapper(false);
			ObjectMapper xmlMapper = new XmlMapper(xmlModule);
			
			CardInfo cardInfo = xmlMapper.readValue(unformattedXmlString, CardInfo.class);
			
			List<String> tags = Stream.concat(resourcesList.stream(), attributesList.stream())
				.map(ecsElement -> sanitizeTag(ecsElement.toString()))
				.collect(Collectors.toList());
			
			if (requiredTags().stream().anyMatch(tags::contains)) {
				throw new UncheckedCardLoadingException("Tags " + requiredTags() + " are required by default, you cannot submit them in the resource or attribute fields.");
			}
			
			List<String> duplicateTags = tags.stream()
				.collect(Collectors.groupingBy(i -> i))
				.entrySet()
				.stream()
				.filter(entry -> entry.getValue().size() > 1)
				.map(Entry::getKey)
				.collect(Collectors.toList());
			
			if (!duplicateTags.isEmpty()) {
				throw new UncheckedCardLoadingException("Tags " + duplicateTags + " have been input multiple times, this is not allowed.");
			}
			
			Map<String, ECSResource> ecsResourcesMap = resourcesList.stream()
				.collect(Collectors.toMap(ecsResource -> sanitizeTag(ecsResource.toString()), i -> i));
			
			Map<String, ECSAttribute> ecsAttributesMap = attributesList.stream()
				.collect(Collectors.toMap(ecsAttribute -> sanitizeTag(ecsAttribute.toString()), i -> i));
			
			List<Card> cardList = cardInfo.getCards().getCards();
			List<String> duplicateIds = cardList.stream()
				.collect(Collectors.groupingBy(Card::getId))
				.entrySet()
				.stream()
				.filter(entry -> entry.getValue().size() > 1)
				.map(Entry::getKey)
				.collect(Collectors.toList());
			
			if (!duplicateIds.isEmpty()) {
				throw new UncheckedCardLoadingException("Card ids " + duplicateIds + " are duplicates, this is not allowed.");
			}
			
			return cardList.stream()
				.map(card -> {
					Entity entity = game.newEntity();
					
					entity.addComponent(new IdComponent(card.getId()));
					
					ECSResourceMap resourceMap = ECSResourceMap.createFor(entity);
					ECSAttributeMap attributeMap = ECSAttributeMap.createFor(entity);
					
					card.getElements().forEach((sanitizedTag, value) -> {
						if (ecsResourcesMap.containsKey(sanitizedTag)) {
							resourceMap.set(ecsResourcesMap.get(sanitizedTag), Integer.parseInt(value.toString()));
						}
						else if (ecsAttributesMap.containsKey(sanitizedTag)) {
							attributeMap.set(ecsAttributesMap.get(sanitizedTag), value.toString());
						}
						else {
							throw new UncheckedCardLoadingException("Element " + sanitizedTag + " has not been found in the supplied resource and attribute mappings where card id = " + card.getId());
						}
					});
					
					return entity;
				})
				.collect(Collectors.toList());
		} catch (UncheckedCardLoadingException ex) {
			throw new CardLoadingException(ex.getMessage(), ex.getCause());
		} catch (Exception ex) {
			throw new CardLoadingException(ex);
		}
	}
	
	private static class CardInfo {
		@JacksonXmlProperty(localName = "Cards")
		private Cards cards;
		
		public Cards getCards() {
			if (cards == null) {
				cards = new Cards();	//fix for Cards instance being null on empty cards list
			}
			return cards;
		}
	}
	
	private static class Cards {
		@JacksonXmlProperty(localName = "Card")
		private Card[] cards;
		
		public List<Card> getCards() {
			if (cards == null) {
				cards = new Card[0];
			}
			return Arrays.asList(cards);
		}
	}
	
	private static class Card {
		private String id;
		private boolean duplicateId = false;
		
		private final Map<String, Object> elements = new HashMap<>();
		
		private boolean duplicateElements = false;
		private final List<String> duplicateElementTags = new ArrayList<>();

		@JsonAnySetter
		private void addElement(final String tag, final Object value) {
			String sanitizedTag = sanitizeTag(tag);
			switch (sanitizedTag) {
				case "id":
					if (id != null) {
						duplicateId = true;
						return;
					}
					id = value.toString();
					break;
				default:
					if (elements.containsKey(sanitizedTag)) {
						duplicateElements = true;
						duplicateElementTags.add(sanitizedTag);
						return;
					}
					elements.put(sanitizedTag, value);
					break;
			}
		}
		
		public String getId() {
			if (duplicateId) {
				throw new UncheckedCardLoadingException("Element id has duplicate entries");
			}
			if (id == null) {
				throw new UncheckedCardLoadingException("Required element id has not been set");
			}
			return id;
		}
		
		@JsonAnyGetter
		public Map<String, Object> getElements() {
			if (duplicateElements) {
				throw new UncheckedCardLoadingException("Elements " + duplicateElementTags + " have duplicate entries where card id = " + id);
			}
			return new HashMap<>(elements);
		}
	}
}
