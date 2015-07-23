println 'test'

def clearState = {
    assert game.getGameState() == com.cardshifter.modapi.base.ECSGameState.RUNNING
    def players = game.players
    assert currentPlayer == null
    for (def ent : players) {
        entity ent uses 'Mulligan' by ent withTargets 0 ok
    }
}

from clearState test 'some test' using {
    def attacker = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 2
        health 2
    }
    def defender = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 1
        health 4
    }

    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'Attack' on attacker withTarget defender ok
    assert attacker.health == 1
    assert defender.health == 2
    uses 'End Turn' ok
    assert attacker.health == 2
    assert defender.health == 4
}

