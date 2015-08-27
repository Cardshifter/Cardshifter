import com.cardshifter.modapi.actions.*
import com.cardshifter.modapi.attributes.*

/* This file contains the card extensions which define Enchantment cards.
 * An Enchantment is a card which is played onto another card (generally that target card is on Battlefield)
 *   which imbues the target with changes in resources, as well as can add certain properties or effects.
 * Depending on the mod's design, an Enchantment can be cast using Mana, Scrap, no resource at all.
 * Also depending on the mod's Game configuration, enchantments may or may not be limited to certain types of creatures as targets.
 * @author Simon Forsberg [code]
 * @author Francis Gaboury [docs] */

include 'spells'

// Define the Enchantments' addAttack value
cardExtension('addAttack') {int value ->
    attack value
}
// Define the Enchantments' addHealth value
cardExtension('addHealth') {int value ->
    health value
}
// Define the Enchantments' scrapCost value
cardExtension('scrapCost') {int value ->
    scrap_cost value
}
// Define the Enchantments' health and max health values
cardExtension('health') {int value ->
    setResource('health', value)
    setResource('max_health', value)
}
// Define Enchantment entity and related actions
cardExtension('enchantment') {
    def entity = entity()
    def actions = entity.getComponent(ActionComponent)
    def enchantAction = new ECSAction(entity, 'Enchant', {act -> true}, {act -> }).addTargetSet(1, 1)

    actions.addAction(enchantAction)
}
// Define Enchantments' set effect
cardExtension('set') {resource, val ->
    def entity = entity()
    def eff = new net.zomis.cardshifter.ecs.effects.Effects();

    entity.addComponent(
        // "Plain English" description for game clients
        eff.described("Set " + resource + " to " + val,
            eff.giveTarget(resource, 1, {i -> val})
        )
    );
}

cardExtension('enchantment2') {
    spell {
        targets 1 cards {
            creature true
            zone 'Battlefield'
        }
    }
}