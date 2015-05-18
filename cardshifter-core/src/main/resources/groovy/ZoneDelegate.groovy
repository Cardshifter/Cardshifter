import com.cardshifter.modapi.actions.ActionComponent
import com.cardshifter.modapi.attributes.Attributes
import com.cardshifter.modapi.attributes.ECSAttributeMap
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ECSResourceMap
import org.codehaus.groovy.control.CompilerConfiguration

class CardDelegate {
    Entity entity
    GroovyMod mod

    Entity entity() {
        entity
    }

    def setResource(String resource, int value) {
        ECSResource res = entity.game.resource(resource)
        assert res : 'No such resource: ' + resource
        res.retriever.set(entity, (int) value)
        println "set $res $resource to $value (setResource)"
    }

    def propertyMissing(String name, value) {
        "$name"(value)
    }

    def propertyMissing(String name) {
        println 'Missing property: ' + name
    }

    def methodMissing(String name, args) {
        ECSResource res = entity.game.resource(name)
        if (res) {
            int value = args[0]
            res.retriever.set(entity, value)
            println "set $res $name to $value (method)"
        } else {
            println 'Missing method: ' + name
        }
    }
}

class ZoneDelegate {
    Entity entity
    ZoneComponent zone
    GroovyMod mod

    def cards(Closure<?> closure) {
        closure.delegate = this
        closure.call()
    }

    def cardset(String name) {
        File file = new File(mod.modDirectory, "${name}.groovy")
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.setScriptBaseClass(DelegatingScript.class.getName())
        GroovyShell sh = new GroovyShell(mod.loader, new Binding(), cc)
        DelegatingScript script = (DelegatingScript) sh.parse(file)
        script.setDelegate(this)

        int size = zone.size()
        script.run()
        println "Include cardset $name: Included ${zone.size() - size} cards"
    }

    def card(String name, Closure<?> closure) {
        def card = entity.game.newEntity()
        ECSAttributeMap.createFor(card).set(Attributes.NAME, name)
        ECSResourceMap.createFor(card)
        card.addComponent(new ActionComponent())
        closure.delegate = new CardDelegate(entity: card, mod: mod)
        closure.setResolveStrategy(Closure.DELEGATE_ONLY)
        closure.call()
        zone.addOnBottom(card)
    }

    def card(Closure<?> closure) {
        card('', closure)
    }
}
