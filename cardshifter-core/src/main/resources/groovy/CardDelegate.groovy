import com.cardshifter.modapi.actions.ActionComponent
import com.cardshifter.modapi.attributes.ECSAttributeMap
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ECSResourceMap
import groovy.transform.PackageScope

class CardDelegate implements GroovyInterceptable {
    Entity entity
    GroovyMod mod

    @PackageScope Entity createCard(Entity card, Closure<?> closure, int resolveStrategy) {
        ECSAttributeMap.createOrGetFor(card)
        ECSResourceMap.createFor(card)
        card.addComponent(new ActionComponent())
        this.entity = card
        closure.delegate = this
        closure.setResolveStrategy(resolveStrategy)
        closure.call()
        return card
    }

    Entity entity() {
        entity
    }

    static def missingMethod(Entity entity, GroovyMod mod, String name, args) {
        ECSResource res = mod.resourceOrNull(name)
        if (res) {
            int value = 1
            if (args.length == 1) {
                Object param = args[0]
                assert param != null : "Invalid parameter when calling $name with args $args for $entity"
                value = param as int
            } else if (args.length > 1) {
                throw new MissingMethodException(name, CardDelegate, (Object[]) args)
            }
            res.retriever.set(entity, value)
        } else {
            println "Missing method: $name on $entity with args $args"
        }
    }

    def invokeMethod(String name, args) {
        def metaMethod = CardDelegate.metaClass.getMetaMethod(name, args)
        def result
        if (metaMethod) {
            result = metaMethod.invoke(this, args)
        } else {
            result = missingMethod(entity, mod, name, args)
        }

        List<Closure> closures = mod.cardMethodListeners.get(name)
        if (closures) {
            closures.each {
                it.setDelegate(this)
                it.call(entity, args)
            }
        }
        return result
    }

    def setResource(String resource, int value) {
        ECSResource res = mod.resource(resource)
        assert res : 'No such resource: ' + resource
        res.retriever.set(entity, (int) value)
    }

    def propertyMissing(String name, value) {
        "$name"(value)
    }

    def propertyMissing(String name) {
        mod.resource(name)
    }

    def methodMissing(String name, args) {
        missingMethod(entity, mod, name, args)
    }
}
