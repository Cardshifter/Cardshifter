println 'test'

def clearState = {
    assert game.getGameState() == com.cardshifter.modapi.base.ECSGameState.RUNNING
    def players = game.players
    assert currentPlayer == null
    for (def ent : players) {
        entity ent uses 'Mulligan' by ent withTargets 0 ok
    }
}

from clearState test 'fight kill and no-kill' using {
    def attacker = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 3
        health 3
    }
    def defender = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 2
        health 3
    }

    uses 'End Turn' ok
    uses 'End Turn' ok

    uses 'Attack' on attacker withTarget defender ok
    assert defender.removed
    assert !attacker.removed
    assert attacker.health == 1
}

from clearState test 'fight kill and no-kill 2/2 vs 1/1' using {
    def attacker = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 2
        health 2
    }
    def defender = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 1
        health 1
    }

    uses 'End Turn' ok
    uses 'End Turn' ok

    uses 'Attack' on attacker withTarget defender ok
    assert defender.removed
    assert !attacker.removed
    assert attacker.health == 1
}

from clearState test 'attack opponent with enchanted rush' using {
    uses 'End Turn' ok

    def enchantedCreature = to you zone 'Battlefield' create {
        creature 'Bio'
        attack 4
        health 4
    }

    def enchantment = to you zone 'Hand' create {
        println 'Sickness is ' + SICKNESS
        enchantment true
        set(SICKNESS, 0)
    }
    expect failure when enchantedCreature uses 'Attack' ok
    uses 'Enchant' on enchantment withTarget enchantedCreature ok

    assert enchantedCreature.sickness == 0

    allowed 'Attack' on enchantedCreature ok

    int originalLife = opponent.health
    uses 'Attack' on enchantedCreature withTarget opponent ok
    assert opponent.health == originalLife - 4

}

from clearState test 'max mana' using {
    for (int i = 0; i < 30; i++) {
        uses 'End Turn' ok
    }
    assert you.mana == 10
}

from clearState test 'max mana' using {
    def healer = to you zone 'Hand' create {
        creature 'Bio'
        health 1
        afterPlay {
            damage 1 to 'you'
        }
        onEndOfTurn {
            heal 1 to 'you'
        }
    }
    def player = you
    assert player.health == 30
    uses 'Play' on healer ok
    assert player.health == 29
    uses 'End Turn' ok
    assert player.health == 30
}

from clearState test 'deny counter_attack' using {
    def creature = {
        creature 'Bio'
        attack 2
        health 4
        deny_counterattack 1
        sickness 0
    }
    def attacker = to you zone 'Battlefield' create creature
    def defender = to opponent zone 'Battlefield' create creature

    int originalLife = attacker.health
    uses 'Attack' on attacker withTarget defender ok
    assert attacker.health == originalLife
}

from clearState test 'negative mana cost' using {
    def superCard = {
        creature 'Mech'
        manaCost 1
        health 1
        whilePresent {
            change MANA_COST by -2 withPriority 1 onCards {
                ownedBy "you"
                zone "Hand"
            }
        }
    }
    def playCard = to you zone 'Hand' create superCard
    def handCard = to you zone 'Hand' create superCard

    assert you.mana == 1
    uses 'Play' on playCard ok
    assert you.mana == 0
    assert handCard.mana_cost == -1
    uses 'Play' on handCard ok
    assert you.mana == 0

}
