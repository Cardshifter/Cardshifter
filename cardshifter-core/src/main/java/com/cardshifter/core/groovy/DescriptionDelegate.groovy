package com.cardshifter.core.groovy

class DescriptionDelegate {

    String stringValue = ''

    static String run(Closure closure) {
        def delegate = new DescriptionDelegate()
        closure = closure.rehydrate(delegate, closure.owner, closure.thisObject)
        closure.call()
        delegate.stringValue
    }

    def propertyMissing(String name, value) {
        stringValue += "$name = $value"
        this
    }

    def propertyMissing(String name) {
        stringValue += name + '.'
        this
    }

    def methodMissing(String name, args) {
        stringValue += "$name($args)"
        this
    }

}
