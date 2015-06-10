import com.cardshifter.modapi.actions.ActionComponent
import com.cardshifter.modapi.attributes.Attributes
import com.cardshifter.modapi.attributes.ECSAttributeMap
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.resources.ECSResourceMap
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
        File file = new File(mod.modDirectory, "${name}.groovy")
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.setScriptBaseClass(DelegatingScript.class.getName())
        GroovyShell sh = new GroovyShell(mod.loader, mod.binding, cc)
        DelegatingScript script = (DelegatingScript) sh.parse(file)
        script.setDelegate(this)

        int size = zone.size()
        script.run()
        println "Include cardset $name: Included ${zone.size() - size} cards"
    }

    def card(String name, Closure<?> closure) {
        def card = entity.game.newEntity()
        ECSAttributeMap.createOrGetFor(card).set(Attributes.NAME, name)
        cardDelegate.createCard(card, closure, Closure.OWNER_FIRST)
        zone.addOnBottom(card)
    }

    def card(Closure<?> closure) {
        card('', closure)
    }
}
