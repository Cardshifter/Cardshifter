// Import required action-related classes
import com.cardshifter.modapi.actions.*

/* The Scrap mechanic is defined in this file.
 * It allows for creatures to have a SCRAP resource value, and a Scrap action accordingly.
 * When an eligible card is scrapped from the Battlefield, it is destroyed in exchange for its SCRAP value granted to the owner.
 * SCRAP resource is used similarly to MANA, e.g., a card can have a scrap_cost rather than, or in addition to, a mana_cost.
 *
 * NOTE: For the Scrap mechanic to be available, the mod's Game.groovy config file must contain: include 'scrap'
 *
 * @author Simon Forsberg [code]
 * @author Francis Gaboury [docs]
 * */

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
    // Define how a card is scrapped.
    action('Scrap') {
        // allow only if...
        allowFor {
            // ...card owned by active player
            ownedBy 'active'
            // ...card is present on Battlefield
            zone 'Battlefield'
        }
        // requiring...
        requires {
            // ...card not having sickness this turn
            require card.sickness == 0
            // ...card having attack_available this turn
            require card.attack_available > 0
            // ...card having a SCRAP value
            require card.scrap > 0
        }
        // Perform upon scrap action:
        perform {
            // add card's SCRAP value to the player's SCRAP stockpile
            card.owner.scrap += card.scrap
            // destroy the scrapped card
            card.destroy()
        }
    }
    /* 1) this action costs SCRAP to play
     * 2) the value it costs is equal to scrap_cost value of the card
     * 3) card.owner indicates that the card's owner should pay this cost */
    action('Enchant') {
        cost SCRAP value { card.scrap_cost } on { card.owner }
    }
    /* 1) this action costs SCRAP to play
     * 2) the value it costs is equal to scrap_cost value of the card
     * 3) card.owner indicates that the card's owner should pay this cost */
    action('Use') {
        cost SCRAP value { card.scrap_cost } on { card.owner }
    }

}
