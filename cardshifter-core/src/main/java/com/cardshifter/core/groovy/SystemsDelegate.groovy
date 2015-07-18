package com.cardshifter.core.groovy

import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.ECSSystem

class SystemsDelegate {
    ECSGame game

    void addSystem(ECSSystem system) {
        game.addSystem(system)
    }

}
