
package com.cardshifter.core.cardloader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.NameComponent;
import com.cardshifter.modapi.cards.CardImageComponent;
import com.cardshifter.modapi.cards.CardTypeComponent;
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
	public Collection<Entity> loadCards(final Path path, final Supplier<Entity> entitySupplier, final ECSResource[] resources) throws CardLoadingException {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(entitySupplier, "entitySupplier");
		Objects.requireNonNull(resources, "resources");
		
		try {
			ObjectMapper xmlMapper = new XmlMapper();
			Cards cards = xmlMapper.readValue(path.toFile(), Cards.class);
			
			Map<String, ECSResource> ecsResourcesMap = Arrays.stream(resources)
				.collect(Collectors.toMap(ECSResource::toString, i -> i));
			
			return cards.getCards().stream()
				.map(card -> {
					Entity entity = entitySupplier.get();
					ECSResourceMap resourceMap = ECSResourceMap.createFor(entity);

					card.getResources().forEach((name, value) -> {
						if (!ecsResourcesMap.containsKey(name)) {
							throw new UncheckedCardLoadingException("Resource " + name + " has not been found in the supplied resource mapping");
						}
						resourceMap.set(ecsResourcesMap.get(name), value);
					});
					
					if (card.getName() != null) {
						entity.addComponent(new NameComponent(card.getName()));
					}
					if (card.getCardType() != null) {
						entity.addComponent(new CardTypeComponent(card.getCardType()));
					}
					if (card.getImage() != null) {
						entity.addComponent(new CardImageComponent(card.getImage()));
					}
					
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
		@JacksonXmlProperty(localName = "Name")
		private String name;

		@JacksonXmlProperty(localName = "Image")
		private String image;

		@JacksonXmlProperty(localName = "CardType")
		private String cardType;

		private final Map<String, Integer> resources = new HashMap<>();

		private boolean duplicateResources = false;
		private final List<String> duplicateResourceNames = new ArrayList<>();

		@JsonAnySetter
		private void addResource(final String name, final Object value) {
			if (resources.containsKey(name)) {
				duplicateResources = true;
				duplicateResourceNames.add(name);
			}
			resources.put(name, Integer.parseInt(value.toString()));
		}

		public String getName() {
			return name;
		}

		public String getImage() {
			return image;
		}

		public String getCardType() {
			return cardType;
		}

		@JsonAnyGetter
		public Map<String, Integer> getResources() {
			if (duplicateResources) {
				throw new UncheckedCardLoadingException("Resources " + duplicateResourceNames + " have duplicate entries");
			}
			return new HashMap<>(resources);
		}
	}
}
