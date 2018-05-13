package com.cardshifter.api.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.cardshifter.api.outgoing.CardInfoMessage;

public class DeckConfig implements PlayerConfig {

	private Map<String, CardInfoMessage> cardData;
	private Map<String, Integer> chosen = new HashMap<String, Integer>();
	private Map<String, Integer> max = new HashMap<String, Integer>();
	private int minSize;
	private int maxSize;
	private int maxPerCard;
	
	public DeckConfig() {
		this(0, 0, new HashMap<String, CardInfoMessage>(), 0);
	}
	public DeckConfig(int minSize, int maxSize, Map<String, CardInfoMessage> cardData, int maxPerCard) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.maxPerCard = maxPerCard;
		this.cardData = new HashMap<String, CardInfoMessage>(cardData);
	}
	
	public void setMax(String id, int max) {
		this.max.put(id, max);
	}
	
	public void setChosen(String id, int chosen) {
		this.chosen.put(id, chosen);
	}
	
	public int getChosen(String id) {
		Integer value = this.chosen.get(id);
		return value == null ? 0 : value;
	}

	public void removeChosen(String id) {
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
	
	public Map<String, CardInfoMessage> getCardData() {
		return Collections.unmodifiableMap(cardData);
	}
	
	public Map<String, Integer> getChosen() {
		return Collections.unmodifiableMap(chosen);
	}
	
	public void clearChosen() {
		chosen.clear();
	}
	
	public Map<String, Integer> getMax() {
		return new HashMap<String, Integer>(max);
	}
	
	public int getMaxPerCard() {
		return maxPerCard;
	}
	
	public int total() {
		int sum = 0;
		for (Integer ee : chosen.values()) {
			sum += ee;
		}
		return sum;
	}
	
	@Override
	public String toString() {
		return "DeckConfig [chosen=" + chosen + "]";
	}
	
	public void generateRandom() {
		Random random = new Random();
		List<String> ids = new ArrayList<String>(this.getCardData().keySet());
		while (this.total() < this.getMinSize()) {
			String randomId = ids.get(random.nextInt(ids.size()));
			this.setChosen(randomId, this.getMaxFor(randomId));
		}
	}
	
	public void add(String cardId) {
		Integer current = chosen.get(cardId);
		if (current == null) {
			current = 0;
		}
		chosen.put(cardId, current + 1);
	}
	
	public int getMaxFor(String id) {
		Integer value = this.max.get(id);
		return value == null ? maxPerCard : value;
	}

    @Override
    public void beforeSend() {
        // Don't send information about cards that cannot be chosen
        for (Map.Entry<String, Integer> ee : this.max.entrySet()) {
            if (ee.getValue() <= 0) {
                this.cardData.remove(ee.getKey());
            }
        }
    }

	@Override
	public void validate(PlayerConfig original) {
		DeckConfig originalDeck = (DeckConfig) original;
		for (Map.Entry<String, Integer> ee : this.chosen.entrySet()) {
			String id = ee.getKey();
			int maxAllowed = originalDeck.getMaxFor(id);
			if (ee.getValue() > maxAllowed) {
				throw new RuntimeException("Invalid amount specified for " + id +
					": Specified " + ee.getValue() + " but max is " + maxAllowed);
			}
		}
	}

}
