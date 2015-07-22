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
