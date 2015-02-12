package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionAllowedCheckEvent;
import com.cardshifter.modapi.attributes.AttributeRetriever;
import com.cardshifter.modapi.attributes.Attributes;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;

import java.util.HashSet;
import java.util.Set;

/**
 * A system that denies a specific action on all entities with a specific name
 */
public class DenyActionForNames implements ECSSystem {

    private final HashSet<String> names;
    private final String action;

    private AttributeRetriever name = AttributeRetriever.forAttribute(Attributes.NAME);

    public DenyActionForNames(String action, Set<String> deniedNames) {
        this.names = new HashSet<>(deniedNames);
        this.action = action;
    }

    @Override
    public void startGame(ECSGame game) {
        game.getEvents().registerHandlerAfter(this, ActionAllowedCheckEvent.class, this::allowCheck);
    }

    private void allowCheck(ActionAllowedCheckEvent event) {
        if (!action.equals(event.getAction().getName())) {
            return;
        }
        if (names.contains(name.getOrDefault(event.getEntity(), ""))) {
            event.setAllowed(false);
        }
    }

}
