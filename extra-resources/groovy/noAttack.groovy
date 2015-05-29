/**
 * Created by Simon on 5/18/2015.
 */
def noAttackCards = [] as Set

// Register a new method when adding cards
CardDelegate.metaClass.noAttack << {
    // add the card name to our set of cards that should not attack
    noAttackCards.add(entity().name)
}

setup {
    systems {
        // add the system that prevents attacks for cards with the given names
        println "Deny attack for: $noAttackCards"
        addSystem new net.zomis.cardshifter.ecs.usage.DenyActionForNames(ATTACK_ACTION, noAttackCards)
    }
}
