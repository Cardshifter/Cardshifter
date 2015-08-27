def clearState = {
    assert game.getGameState() == com.cardshifter.modapi.base.ECSGameState.RUNNING
    def players = game.players
    assert currentPlayer == null
    for (def ent : players) {
        entity ent uses 'Mulligan' by ent withTargets 0 ok
    }
}

from clearState test 'spellcards' using {
    def spell = to you zone 'Battlefield' create 'Random Do Nothing'
    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'End Turn' ok

}

from clearState test 'trample' using {
    def trample = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 5
        health 5
        sickness 0
        trample 1
    }
    def defender = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 4
        health 3
    }
    assert opponent.health == 30
    uses 'Attack' on trample withTarget defender ok
    assert opponent.health == 28
    assert !trample.removed
    assert defender.removed
}

from clearState test 'trample not for defender' using {
    def trampleCreature = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 5
        health 5
        sickness 0
        trample 1
    }
    def defender = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 9
        health 3
        trample 1
    }
    assert you.health == 30
    assert opponent.health == 30
    uses 'Attack' on trampleCreature withTarget defender ok
    assert you.health == 30
    assert opponent.health == 28
    assert trampleCreature.removed
    assert defender.removed
}

from clearState test 'spellcards' using {
    def spell = to you zone 'Hand' create 'Destroy Spell'
    def targetCreature = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 1
        health 4
    }

    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'Use' on spell withTarget targetCreature ok
    assert targetCreature.removed
    assert spell.removed
}

from clearState test 'Pick One Change' using {
    def card = to you zone 'Battlefield' create 'Pick One Change'

    assert card.attack == 0
    assert card.health == 2
    uses 'End Turn' ok
    assert card.attack == 1 || card.health == 3 || card.sickness == 2

    uses 'End Turn' ok
}

from clearState test 'enchantment with afterPlay' using {
    def enchant = to you zone 'Hand' create {
        enchantment2()
        afterPlay {
            change ATTACK by 1 on targets
        }
    }
    def creatur = to you zone 'Battlefield' create {
        creature 'Bio'
        attack 1
        health 1
    }

    assert creatur.attack == 1
    uses 'Use' on enchant withTarget creatur ok
    assert creatur.attack == 2
}
