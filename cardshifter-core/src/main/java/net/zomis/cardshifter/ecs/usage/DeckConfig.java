package net.zomis.cardshifter.ecs.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.zomis.cardshifter.ecs.EntitySerialization;

import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.modapi.base.Entity;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class DeckConfig {

	private final Map<Integer, CardInfoMessage> cardData = new HashMap<>();
	private final Map<Integer, Integer> chosen = new HashMap<>();
	private final Map<Integer, Integer> max = new HashMap<>();
	private final int minSize;
	private final int maxSize;
	private final int maxPerCard;
	
	@JsonCreator
	DeckConfig() {
		this(0, 0, new ArrayList<>(), 0);
	}
	public DeckConfig(int minSize, int maxSize, List<Entity> cardOptions, int maxPerCard) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.maxPerCard = maxPerCard;
		
		for (Entity card : cardOptions) {
			cardData.put(card.getId(), EntitySerialization.serialize(0, card));
		}
		
	}
	
	public void setMax(Entity entity, int max) {
		this.max.put(entity.getId(), max);
	}
	
	public void setChosen(int id, int chosen) {
		this.chosen.put(id, chosen);
	}
	
	public int getMinSize() {
		return minSize;
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	
	public Map<Integer, CardInfoMessage> getCardData() {
		return Collections.unmodifiableMap(cardData);
	}
	
	public Map<Integer, Integer> getChosen() {
		return Collections.unmodifiableMap(chosen);
	}
	
	public Map<Integer, Integer> getMax() {
		return new HashMap<>(max);
	}
	
	public int getMaxPerCard() {
		return maxPerCard;
	}

}
