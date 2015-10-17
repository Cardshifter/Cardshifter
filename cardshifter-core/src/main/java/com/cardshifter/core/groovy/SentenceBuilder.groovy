package com.cardshifter.core.groovy

/**
 * Build sentences using a convenient DSL. Sentences are capitalized and end with a period.
 */
class SentenceBuilder {

    private StringBuilder stringBuilder = new StringBuilder()

    static String build(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = SentenceBuilder) Closure closure) {
        def builder = new SentenceBuilder()
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()

        return builder.toString()
    }

    String toString() {
        String sentence = stringBuilder.toString()

        sentence = sentence.trim()
        if (sentence.length() > 0 && !sentence.endsWith('.')) {
            sentence = sentence + '.'
        }
        sentence = sentence.capitalize()

        return sentence
    }

    void text(String strings) {
        stringBuilder.append(strings)
    }

    /**
     * Add a separator if not already present, e.g. ' ' or ', '.
     */
    void separator(String sep) {
        if (stringBuilder.length() > 0 && !stringBuilder.toString().endsWith(sep)) {
            stringBuilder.append(sep)
        }
    }

}
