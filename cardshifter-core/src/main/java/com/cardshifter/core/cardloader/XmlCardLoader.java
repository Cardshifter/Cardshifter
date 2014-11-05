
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
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cardshifter.core.cardloader.CardLoaderHelper.*;
import com.cardshifter.modapi.attributes.ECSAttribute;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.IdComponent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * A card loader to load cards from XML files.
 *
 * @author Frank van Heeswijk
 */
public class XmlCardLoader implements CardLoader<Path> {
	@Override
	public Collection<Entity> loadCards(final Path path, final Supplier<Entity> entitySupplier, final ECSResource[] resources, final ECSAttribute[] attributes) throws CardLoadingException {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(entitySupplier, "entitySupplier");
		
		ECSResource[] resourcesCopy = (resources == null) ? new ECSResource[0] : resources;
		ECSAttribute[] attributesCopy = (attributes == null) ? new ECSAttribute[0] : attributes;
		
		try {
			ObjectMapper xmlMapper = new XmlMapper();
			Cards cards = xmlMapper.readValue(path.toFile(), Cards.class);
			
			List<String> tags = Stream.concat(Arrays.stream(resourcesCopy), Arrays.stream(attributesCopy))
				.map(ecsElement -> sanitizeTag(ecsElement.toString()))
				.collect(Collectors.toList());
			
			if (requiredTags().stream().anyMatch(requiredTag -> tags.contains(requiredTag))) {
				throw new UncheckedCardLoadingException("Tags " + requiredTags() + " are required by default you cannot submit them in the resources or attributes.");
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
			
			Map<String, ECSResource> ecsResourcesMap = Arrays.stream(resourcesCopy)
				.collect(Collectors.toMap(ecsResource -> sanitizeTag(ecsResource.toString()), i -> i));
			
			Map<String, ECSAttribute> ecsAttributesMap = Arrays.stream(attributesCopy)
				.collect(Collectors.toMap(ecsAttribute -> sanitizeTag(ecsAttribute.toString()), i -> i));
			
			List<Card> cardList = cards.getCards();
			List<String> duplicateIds = cardList.stream()
				.collect(Collectors.groupingBy(Card::getId))
				.entrySet()
				.stream()
				.filter(entry -> entry.getValue().size() > 1)
				.map(Entry::getKey)
				.collect(Collectors.toList());
			
			if (!duplicateIds.isEmpty()) {
				throw new UncheckedCardLoadingException("Card ids " + duplicateIds + " are duplicaties, this is not allowed.");
			}
			
			return cardList.stream()
				.map(card -> {
					Entity entity = entitySupplier.get();
					
					entity.addComponent(new IdComponent(card.getId()));
					
					ECSResourceMap resourceMap = ECSResourceMap.createFor(entity);
					ECSAttributeMap attributeMap = ECSAttributeMap.createFor(entity);
					
					card.getResources().forEach((sanitizedTag, value) -> {
						if (!ecsResourcesMap.containsKey(sanitizedTag)) {
							throw new UncheckedCardLoadingException("Resource " + sanitizedTag + " has not been found in the supplied resource mapping");
						}
						resourceMap.set(ecsResourcesMap.get(sanitizedTag), value);
					});

					card.getAttributes().forEach((sanitizedTag, value) -> {
						if (!ecsAttributesMap.containsKey(sanitizedTag)) {
							throw new UncheckedCardLoadingException("Attribute " + sanitizedTag + " has not been found in the supplied attribute mapping");
						}
						attributeMap.set(ecsAttributesMap.get(sanitizedTag), value);
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

	@JacksonXmlRootElement(localName = "Cards")
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Cards {
		@JacksonXmlProperty(localName = "Card")
		@JacksonXmlElementWrapper(useWrapping = false)
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

		private final Map<String, Integer> resources = new HashMap<>();
		private final Map<String, String> attributes = new HashMap<>();

		private boolean duplicateResources = false;
		private final List<String> duplicateResourceTags = new ArrayList<>();

		private boolean duplicateAttributes = false;
		private final List<String> duplicateAttributeTags = new ArrayList<>();

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
					try {
						int intValue = Integer.parseInt(value.toString());
						addResource(sanitizedTag, intValue);
					} catch (NumberFormatException ex) {
						addAttribute(sanitizedTag, value.toString());
					}
					break;
			}
		}
		
		private void addResource(final String sanitizedTag, final int value) {
			if (resources.containsKey(sanitizedTag)) {
				duplicateResources = true;
				duplicateResourceTags.add(sanitizedTag);
				return;
			}
			resources.put(sanitizedTag, value);
		}
		
		private void addAttribute(final String sanitizedTag, final String value) {
			if (attributes.containsKey(sanitizedTag)) {
				duplicateAttributes = true;
				duplicateAttributeTags.add(sanitizedTag);
				return;
			}
			attributes.put(sanitizedTag, value);
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
		public Map<String, Integer> getResources() {
			if (duplicateResources) {
				throw new UncheckedCardLoadingException("Resources " + duplicateResourceTags + " have duplicate entries");
			}
			return new HashMap<>(resources);
		}

		@JsonAnyGetter
		public Map<String, String> getAttributes() {
			if (duplicateAttributes) {
				throw new UncheckedCardLoadingException("Attributes " + duplicateAttributeTags + " have duplicate entries");
			}
			return new HashMap<>(attributes);
		}
	}
}
