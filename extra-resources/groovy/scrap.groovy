// Import required action-related classes
import com.cardshifter.modapi.actions.*

// Add a hook when a card is set to be a creature
onCard('creature') {entity, args ->
    // Get the action component
    def actions = entity.getComponent(ActionComponent)

    // Create a scrap action
    def scrapAction = new ECSAction(entity, 'Scrap', {act -> true }, {act -> })

    // Add the action to the action component
    actions.addAction(scrapAction)

// TODO: Change to:   entity.actions.add('Scrap')
}

setup {

    systems {
        // Scrap
        ScrapSystem(SCRAP, {entity ->
            return ATTACK_AVAILABLE.retriever.getOrDefault(entity, 0) > 0 &&
                    SICKNESS.retriever.getOrDefault(entity, 1) == 0;
        })
        useCost(action: ENCHANT_ACTION, res: SCRAP, value: SCRAP_COST, whoPays: "player")
        useCost(action: USE_ACTION, res: SCRAP, value: SCRAP_COST, whoPays: "player")
    }
}
