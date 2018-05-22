package com.cardshifter.core.groovy

import com.cardshifter.modapi.base.Entity
import net.zomis.cardshifter.ecs.effects.TargetFilter

class GroovyFilter {

    TargetFilter predicate
    String description

    static GroovyFilter getIdentity() {
        new GroovyFilter(
            predicate: { Entity source, Entity target -> true },
            description: ''
        )
    }

}
