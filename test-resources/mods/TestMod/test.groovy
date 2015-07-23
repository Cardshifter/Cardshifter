println 'test'

def clearState = {
    assert game.getGameState() == com.cardshifter.modapi.base.ECSGameState.RUNNING
    def players = game.players
    assert currentPlayer == null
    for (def ent : players) {
        entity ent uses 'Mulligan' by ent withTargets 0 ok
    }
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

