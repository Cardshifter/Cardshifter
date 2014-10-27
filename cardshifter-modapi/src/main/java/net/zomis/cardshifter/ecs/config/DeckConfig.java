package net.zomis.cardshifter.ecs.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.zomis.cardshifter.ecs.EntitySerialization;

import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.modapi.base.Entity;

public class DeckConfig {

	private final Map<Integer, CardInfoMessage> cardData = new HashMap<>();
	private final Map<Integer, Integer> chosen = new HashMap<>();
	private final Map<Integer, Integer> max = new HashMap<>();
	private final int minSize;
	private final int maxSize;
	private final int maxPerCard;
	
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
	
	public void removeChosen(int id) {
		if (this.chosen.get(id) > 1) {
			this.setChosen(id, this.getChosen().get(id) - 1);
		} else {
			this.chosen.remove(id);
		}
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
	
	public int total() {
		return chosen.values().stream().mapToInt(i -> i).sum();
	}
	
	@Override
	public String toString() {
		return "DeckConfig [chosen=" + chosen + "]";
	}
	
	public void generateRandom() {
		Random random = new Random();
		List<Integer> ids = new ArrayList<>(this.getCardData().keySet());
		while (this.total() < this.getMinSize()) {
			int randomId = ids.get(random.nextInt(ids.size()));
			this.setChosen(randomId, this.getMaxFor(randomId));
		}
	}
	
	public void add(int cardId) {
		chosen.merge(cardId, 1, (a, b) -> a + b);
	}
	
	public int getMaxFor(int id) {
		return this.max.getOrDefault(id, maxPerCard);
	}

}
