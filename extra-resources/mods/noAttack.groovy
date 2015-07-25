/* Defines the noAttack() card property, which results in a card not being able to Attack, regardless of its ATTACK resource.
 * Note that a card with noAttack() can still counterattack and cause damage when it is being attacked by an opponent creature.
 * @author Simon Forsberg [code]
 * @author Francis Gaboury [docs]
 */

def noAttackCards = new HashSet<>()

// Register a new method when adding cards
cardExtension('noAttack') {
    // add the card name to our set of cards that should not attack
    def name = entity().name
    // assert that the name is not null
    assert name : 'Cannot add null name to noAttack cards'
    // add cards with no attack to the set
    noAttackCards.add(name)
}

setup {
    systems {
        // add the system that prevents attacks for cards with the given names
        println "Deny attack for: $noAttackCards"
        addSystem new net.zomis.cardshifter.ecs.usage.DenyActionForNames(ATTACK_ACTION, noAttackCards)
    }
}
