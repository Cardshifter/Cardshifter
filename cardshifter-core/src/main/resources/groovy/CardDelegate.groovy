import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.resources.ECSResource

class CardDelegate implements GroovyInterceptable {
    Entity entity
    GroovyMod mod

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
                throw new MissingMethodException("Method with name $name not found", getClass(), (Object[]) args)
            }
            res.retriever.set(entity, value)
            println "set $res $name to $value (method)"
        } else {
            println 'Missing method: ' + name
        }
    }

    def invokeMethod(String name, args) {
        def metaMethod = CardDelegate.metaClass.getMetaMethod(name, args)
        def result
        if (metaMethod) {
            System.out.println "Invoke method: $name"
            result = metaMethod.invoke(this, args)
            System.out.println "method invocation done."
        } else {
            System.out.println "Invoke method: $name --- missing"
            result = missingMethod(entity, mod, name, args)
        }

        List<Closure> closures = mod.cardMethodListeners.get(name)
        System.out.println "Card listeners for $name: $closures"
        if (closures) {
            System.out.println "Calling ${closures.size()} onCard listeners: $name"
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
        System.out.println "set $res $resource to $value (setResource)"
    }

    def propertyMissing(String name, value) {
        println "property missing, redirecting to method: $name = $value"
        "$name"(value)
    }

/*    def propertyMissing(String name) {
        println 'Missing property: ' + name
    }*/

    def methodMissing(String name, args) {
        missingMethod(entity, mod, name, args)
    }
}
