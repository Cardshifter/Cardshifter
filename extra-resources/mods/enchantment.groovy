import com.cardshifter.modapi.actions.*
import com.cardshifter.modapi.attributes.*

/* An enchantment is a card which is played onto a creature on the Battlefield. It can imbue the target with changes in
 * resources, as well as other effects spells can make use of.
 * Depending on the mod's design, an enchantment can be cast using Mana, Scrap, no resource at all.
 */

include 'spells'

cardExtension('enchantment') {
    spell('Enchant', {
        targets 1 cards {
            creature true
            zone 'Battlefield'
            ownedBy 'you'
        }
    })
}
