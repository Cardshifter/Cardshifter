package com.cardshifter.modapi.resources;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.base.ComponentRetriever;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;

import java.util.Set;

/**
 * A System to recount resources if required after an action has been performed.
 */
public class ResourceRecountSystem implements ECSSystem {

    private ComponentRetriever<ResourceModifierComponent> modifier =
        ComponentRetriever.singleton(ResourceModifierComponent.class);

    @Override
    public void startGame(ECSGame game) {
        game.getEvents().registerHandlerAfter(this, ActionPerformEvent.class, this::recount);
    }

    private void recount(ActionPerformEvent event) {
        ResourceModifierComponent mod = modifier.get(event.getEntity());
        int count = mod.getModifiedResourcesCount();
        if (count == 0) {
            return;
        }

        Set<Entity> entities = event.getEntity().getGame()
            .getEntitiesWithComponent(ECSResourceMap.class);
        entities.stream()
            .map(e -> e.getComponent(ECSResourceMap.class))
            .forEach(e -> mod.getModifiedResources()
                .forEach(res -> e.get(res).ifPresent(resData -> resData.get())));
        System.out.println("Recounted " + count + " resources for " + entities.size() + " entities.");
    }

}
