package com.cardshifter.modapi.cards;

import com.cardshifter.api.config.DeckConfig;
import com.cardshifter.modapi.attributes.Attributes;
import com.cardshifter.modapi.attributes.ECSAttributeData;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import net.zomis.cardshifter.ecs.config.ConfigComponent;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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
        for (Map.Entry<String, Integer> chosen : deckConf.getChosen().entrySet()) {
            String cardId = chosen.getKey();
            int count = chosen.getValue();

            for (int i = 0; i < count; i++) {
                Stream<Entity> entities = game.getEntitiesWithComponent(ECSAttributeMap.class).stream().filter(e ->
                    e.getComponent(ECSAttributeMap.class).get(Attributes.ID)
                        .map(ECSAttributeData::get)
                        .orElse("")
                        .equals(cardId)
                    );

                Entity existing = entities.findFirst().orElseThrow(() -> new RuntimeException("Unable to find an entity with id " + cardId));
                Entity copy = existing.copy();
                deck.addOnBottom(copy);
            }
        }
    }

}
