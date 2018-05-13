package net.zomis.cardshifter.ecs.config;

import com.cardshifter.api.config.DeckConfig;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.modapi.attributes.Attributes;
import com.cardshifter.modapi.attributes.ECSAttributeMap;
import com.cardshifter.modapi.base.Entity;
import net.zomis.cardshifter.ecs.EntitySerialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeckConfigFactory {

    public static DeckConfig create(int minSize, int maxSize, List<Entity> cards, int maxCardsPerType) {
        Map<String, CardInfoMessage> map = new HashMap<>();
        for (Entity entity : cards) {
            String id = ECSAttributeMap.createOrGetFor(entity).getAttribute(Attributes.ID).get();
            map.put(id, EntitySerialization.serialize(0, entity));
        }
        return new DeckConfig(minSize, maxSize, map, maxCardsPerType);
    }
}
