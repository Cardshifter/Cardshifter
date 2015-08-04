package com.cardshifter.core.groovy

import com.cardshifter.modapi.base.ComponentRetriever
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.phase.PhaseChangeEvent
import com.cardshifter.modapi.phase.PhaseController

/**
 * Created by Simon on 6/24/2015.
 */
class EventYouDelegate {
    final PhaseChangeEvent event
    final Entity you

    def EventYouDelegate(PhaseChangeEvent event) {
        this.event = event
        PhaseController phaseController = ComponentRetriever.singleton(event.game, PhaseController)
        this.you = phaseController.currentEntity
    }
}
