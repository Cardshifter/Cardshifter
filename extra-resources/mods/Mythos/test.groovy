package Mythos

def clearState = {
    def players = game.players
    assert currentPlayer == null
    for (def ent : players) {
        entity ent uses 'Mulligan' by ent withTargets 0 ok
    }
    assert currentPlayer
}

from clearState test('summon on battlefield') using {
    def card = to you zone 'Hand' create {
        creature "Chinese"
        health 1
        manaCost 0
        afterPlay {
            damage 1 on { player true }
            summon 1 of "Life Tool" to "you" zone "Battlefield"
        }
    }
    def player = you
    assert player.battlefield.size() == 0
    uses 'Play' on card ok
    assert player.battlefield.size() == 2
    assert player.health == 29
    assert opponent.health == 29
    uses 'End Turn' ok
    assert player.health == 30
}

from clearState test('mana upkeep default') using {
    def cardWithUpkeepDivisibleByFive = to you zone 'Hand' create {
        creature 'Chinese'
        health 1
        manaCost 10
    }
    assert cardWithUpkeepDivisibleByFive.mana_upkeep == 5

    def cardWithEvenNumberUpkeep = to you zone 'Hand' create {
        creature 'Chinese'
        health 1
        manaCost 4
    }
    assert cardWithEvenNumberUpkeep.mana_upkeep == 2

    def cardWithOddNumberUpkeep = to you zone 'Hand' create {
        creature 'Chinese'
        health 1
        manaCost 3
    }
    assert cardWithOddNumberUpkeep.mana_upkeep == 2

    def player = you
    assert player.mana == 10
    uses 'Play' on cardWithOddNumberUpkeep ok
    assert player.mana == 7
    uses 'End Turn' ok
    uses 'End Turn' ok
    assert player.mana == 18
}

from clearState test('mana upkeep with manaUpkeep') using {
    def card = to you zone 'Hand' create {
        creature 'Chinese'
        health 1
        manaCost 3
        manaUpkeep 5
    }
    assert card.mana_upkeep == 5
    def player = you
    assert player.mana == 10
    uses 'Play' on card ok
    assert player.mana == 7
    uses 'End Turn' ok
    uses 'End Turn' ok
    assert player.mana == 15
}