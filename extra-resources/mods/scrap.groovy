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
        allowFor {             // allow only if...
            ownedBy 'active'   // ...card owned by active player
            zone 'Battlefield' // ...card is present on Battlefield
        }
        requires {                            // requiring...
            require card.sickness == 0        // ...card not having sickness this turn
            require card.attack_available > 0 // ...card having attack_available this turn
            require card.scrap > 0            // ...card having a SCRAP value
        }

        perform {  // perform upon Scrap action:
            card.owner.scrap += card.scrap // add card's SCRAP value to the player's SCRAP stockpile
            card.destroy()                 // destroy the scrapped card
        }
    }
    // 1) this action costs SCRAP to play
    // 2) the value it costs is equal to scrap_cost value of the card
    // 3) card.owner indicates that the card's owner should pay this cost
    action('Enchant') {
        cost SCRAP value { card.scrap_cost } on { card.owner }
    }
    // 1) this action costs SCRAP to play
    // 2) the value it costs is equal to scrap_cost value of the card
    // 3) card.owner indicates that the card's owner should pay this cost
    action('Use') {
        cost SCRAP value { card.scrap_cost } on { card.owner }
    }

}
