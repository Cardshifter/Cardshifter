package com.cardshifter.core.groovy

import com.cardshifter.modapi.attributes.Attributes
import com.cardshifter.modapi.attributes.ECSAttributeMap
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.ZoneComponent
import org.codehaus.groovy.control.CompilerConfiguration

class ZoneDelegate {
    Entity entity
    ZoneComponent zone
    GroovyMod mod
    CardDelegate cardDelegate

    def cards(Closure<?> closure) {
        closure.delegate = this
        closure.call()
    }

    def cardset(String name) {
        File file = new File(mod.modDirectory, "${name}.cardset")
        int size = zone.size()
        mod.scriptRunner.runScript(file, this)
        println "Include cardset $name: Included ${zone.size() - size} cards"
    }

    def card(String name, Closure<?> closure) {
        def card = entity.game.newEntity()
        def attributes = ECSAttributeMap.createOrGetFor(card)
        attributes.set(Attributes.NAME, name)
        attributes.set(Attributes.ID, name.toLowerCase().replaceAll(" ", "-"))
        cardDelegate.createCard(card, closure, Closure.OWNER_FIRST)
        zone.addOnBottom(card)
    }

    def card(Closure<?> closure) {
        card('', closure)
    }
}
