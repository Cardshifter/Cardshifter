package net.zomis.cardshifter.ecs.config;

import com.cardshifter.api.config.DeckConfig;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.modapi.base.Entity;
import net.zomis.cardshifter.ecs.EntitySerialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeckConfigFactory {

    public static DeckConfig create(int minSize, int maxSize, List<Entity> cards, int maxCardsPerType) {
        Map<Integer, CardInfoMessage> map = new HashMap<>();
        for (Entity entity : cards) {
            map.put(entity.getId(), EntitySerialization.serialize(0, entity));
        }
        return new DeckConfig(minSize, maxSize, map, maxCardsPerType);
    }
}
