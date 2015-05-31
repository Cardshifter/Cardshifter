println 'test'

def clearState = {
    assert game.getGameState() == com.cardshifter.modapi.base.ECSGameState.RUNNING
    def players = game.players
    assert currentPlayer == null
    for (def ent : players) {
        entity ent uses 'Mulligan' by ent withTargets 0 ok
    }
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

