package mythos

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
