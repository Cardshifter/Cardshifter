package com.cardshifter.api.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.cardshifter.api.outgoing.CardInfoMessage;

public class DeckConfig implements PlayerConfig {

	private Map<Integer, CardInfoMessage> cardData;
	private Map<Integer, Integer> chosen = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> max = new HashMap<Integer, Integer>();
	private int minSize;
	private int maxSize;
	private int maxPerCard;
	
	public DeckConfig() {
		this(0, 0, new HashMap<Integer, CardInfoMessage>(), 0);
	}
	public DeckConfig(int minSize, int maxSize, Map<Integer, CardInfoMessage> cardData, int maxPerCard) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.maxPerCard = maxPerCard;
		this.cardData = new HashMap<Integer, CardInfoMessage>(cardData);
	}
	
	public void setMax(int id, int max) {
		this.max.put(id, max);
	}
	
	public void setChosen(int id, int chosen) {
		this.chosen.put(id, chosen);
	}
	
	public int getChosen(int id) {
		Integer chosen = this.chosen.get(id);
		return chosen == null ? 0 : chosen;
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
	
	public void clearChosen() {
		chosen.clear();
	}
	
	public Map<Integer, Integer> getMax() {
		return new HashMap<Integer, Integer>(max);
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
		List<Integer> ids = new ArrayList<Integer>(this.getCardData().keySet());
		while (this.total() < this.getMinSize()) {
			int randomId = ids.get(random.nextInt(ids.size()));
			this.setChosen(randomId, this.getMaxFor(randomId));
		}
	}
	
	public void add(int cardId) {
		Integer current = chosen.get(cardId);
		if (current == null) {
			current = 0;
		}
		chosen.put(cardId, current + 1);
	}
	
	public int getMaxFor(int id) {
		Integer value = this.max.get(id);
		return value == null ? maxPerCard : value;
	}

    @Override
    public void beforeSend() {
        // Don't send information about cards that cannot be chosen
        for (Map.Entry<Integer, Integer> ee : this.max.entrySet()) {
            if (ee.getValue() <= 0) {
                this.cardData.remove(ee.getKey());
            }
        }
    }
}
