package com.cardshifter.core.groovy

import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.ECSSystem
import com.cardshifter.modapi.cards.CardComponent
import com.cardshifter.modapi.cards.DeckComponent
import com.cardshifter.modapi.cards.DiscardComponent
import com.cardshifter.modapi.cards.DrawCardEvent

class ReshuffleWhenOutOfCardsSystem implements ECSSystem {

    @Override
    void startGame(ECSGame game) {
        game.getEvents().registerHandlerAfter(this, DrawCardEvent.class, { e -> this.perform(e)})
    }

    def perform(DrawCardEvent event) {
        def deck = event.owner.getComponent(DeckComponent.class)
        if (!deck.isEmpty()) {
            return
        }
        def discard = event.owner.getComponent(DiscardComponent.class)
        discard.shuffle()
        discard.forEach({e -> e.getComponent(CardComponent.class).moveToBottom(deck)})
    }

}
