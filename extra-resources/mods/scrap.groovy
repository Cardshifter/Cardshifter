// Import required action-related classes
import com.cardshifter.modapi.actions.*

// Value of Scrap gained when a scrappable card is scrapped from Battlefield
// NOTE: The SCRAP value also applies to players' owned scrap, depending on how you target your effects
SCRAP = createResource("SCRAP")
// Cost of Scrap resource to the player for casting the card into play
SCRAP_COST = createResource("SCRAP_COST")

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

rules {
    action('Scrap') {
        allowFor {
            ownedBy 'active'
            zone 'Battlefield'
        }
        requires {
            require card.sickness == 0
            require card.attack_available > 0
            require card.scrap > 0
        }

        perform {
            card.owner.scrap += card.scrap
            card.destroy()
        }
    }

    action('Enchant') {
        cost SCRAP value { card.scrap_cost } on { card.owner }
    }

    action('Use') {
        cost SCRAP value { card.scrap_cost } on { card.owner }
    }

}
