def clearState = {
    assert game.getGameState() == com.cardshifter.modapi.base.ECSGameState.RUNNING
    def players = game.players
    assert currentPlayer == players[0]
    assert you.castle == 25
    assert you.wall == 15

    assert you.bricks == 8
    assert you.builders == 2
    assert you.weapons == 8
    assert you.recruiters == 2
    assert you.crystals == 8
    assert you.wizards == 2
}

from clearState test 'using builder' using {
    def player = you
    def handCard = to player zone 'Hand' create 'Builder'

    assert player.bricks == 8
    assert player.builders == 2
    uses 'Play' on handCard ok

    assert player.bricks == 0
    assert player.builders == 3
}

from clearState test 'discard 1 card' using {
    def discardMe = to you zone 'Hand' create 'Builder'
    def drawMe = to you zone 'Deck' create 'Builder'
    def remainInDeck = to you zone 'Deck' create 'Builder'
    def player = you

    int currentHandSize = player.hand.size()
    assert player.discard.size() == 0
    uses 'Discard' on you withTarget discardMe ok
    assert player.discard.size() == 1
    assert player.hand.size() == currentHandSize
    assert drawMe.zone.name == 'Hand'
    assert remainInDeck.zone.name == 'Deck'
}

from clearState test 'refill deck' using {
    to you zone 'Hand' create 'Builder'
    to you zone 'Hand' create 'Builder'
    to you zone 'Deck' create 'Builder'

    to opponent zone 'Hand' create 'Builder'
    to opponent zone 'Hand' create 'Builder'
    to opponent zone 'Deck' create 'Builder'

    for (int i = 0; i < 50; i++) {
        def player = you
        def discardMe = you.hand.topCard
        def drawMe = you.deck.topCard

        println "Discarding $i: $discardMe expected draw $drawMe"
        int currentHandSize = player.hand.size()
        assert player.discard.size() == 0
        uses 'Discard' on you withTarget discardMe ok
        // Discard pile should be reshuffled
        assert player.discard.size() == 0
        assert player.deck.size() == 1
        assert player.hand.size() == currentHandSize
        assert drawMe.zone.name == 'Hand'
        assert discardMe.zone.name == 'Deck'


    }
}

from clearState test 'attack wall first then castle' using {
    def attacker = you
    def attackCard = to attacker zone 'Hand' create 'Catapult'
    def defender = opponent

    you.bricks = 30

    defender.castle = 10
    defender.wall = 5

    def oldTotal = opponent.castle + opponent.wall

    assert attackCard.attack == 12
    assert defender.castle == 10
    assert defender.wall == 5
    uses 'Play' on attackCard ok

    assert defender.wall == 0
    assert defender.castle == 3
    assert defender.wall + defender.castle == oldTotal - attackCard.attack
}
