package com.cardshifter.modapi.cards;

import com.cardshifter.api.config.DeckConfig;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import net.zomis.cardshifter.ecs.config.ConfigComponent;

import java.util.Map;

public class DeckComponent extends ZoneComponent {

	public DeckComponent(Entity owner) {
		super(owner, "Deck");
	}

    public void createFromConfig(String name) {
        ConfigComponent playerConfig = getOwner().getComponent(ConfigComponent.class);
        DeckConfig config = (DeckConfig) playerConfig.getConfigs().get(name);
        setupDeck(this, config);
    }

    private static void setupDeck(DeckComponent deck, DeckConfig deckConf) {
        ECSGame game = deck.getOwner().getGame();
        for (Map.Entry<Integer, Integer> chosen : deckConf.getChosen().entrySet()) {
            int entityId = chosen.getKey();
            int count = chosen.getValue();

            for (int i = 0; i < count; i++) {
                Entity existing = game.getEntity(entityId);
                Entity copy = existing.copy();
                deck.addOnBottom(copy);
            }
        }
    }

}
