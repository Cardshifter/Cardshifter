package com.cardshifter.core.groovy

import com.cardshifter.modapi.actions.ActionComponent
import com.cardshifter.modapi.attributes.ECSAttributeMap
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ECSResourceMap
import groovy.transform.PackageScope

class CardDelegate implements GroovyInterceptable {
    Entity entity
    GroovyMod mod
    private final Map<String, Closure> extMethods = [:]

    @PackageScope Entity createCard(Entity card, Closure<?> closure, int resolveStrategy) {
        ECSAttributeMap.createOrGetFor(card)
        ECSResourceMap.createFor(card)
        card.addComponent(new ActionComponent())
        this.entity = card
        closure.delegate = this
        closure.setResolveStrategy(resolveStrategy)
        closure.call()
        def closures = mod.cardMethodListeners.get('#after');
        if (closures) {
            closures.each {
                it.delegate = this
                it.setResolveStrategy(resolveStrategy)
                it.call(entity)
            }
        }
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
        def result = null
        if (metaMethod) {
            result = metaMethod.invoke(this, args)
        } else {
            def cl = this.extMethods.get(name)
            if (cl) {
                cl.delegate = this
                //cl.resolveStrategy = Closure.DELEGATE_FIRST
                // This is ugly, but it works. I have not been able to make this work in a cleaner way
                if (args.length == 0) {
                    result = cl.call()
                } else if (args.length == 1) {
                    result = cl.call(args[0])
                } else if (args.length == 2) {
                    result = cl.call(args[0], args[1])
                } else if (args.length == 3) {
                    result = cl.call(args[0], args[1], args[2])
                } else if (args.length == 4) {
                    result = cl.call(args[0], args[1], args[2], args[3])
                } else {
                    throw new IllegalArgumentException("too many arguments for calling method " + name);
                }
            } else {
                result = missingMethod(entity, mod, name, args)
            }
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

    void extMethod(String methodName, Closure closure) {
        extMethods.put(methodName, closure)
    }
}
