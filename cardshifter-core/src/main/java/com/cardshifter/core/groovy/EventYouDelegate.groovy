package com.cardshifter.core.groovy

import com.cardshifter.modapi.phase.PhaseChangeEvent

/**
 * Created by Simon on 6/24/2015.
 */
class EventYouDelegate {
    PhaseChangeEvent event

    def EventYouDelegate(PhaseChangeEvent event) {
        this.event = event
        this.you = event.newPhase.owner.game.currentPlayer
    }
}
