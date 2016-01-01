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

    /**
     * Add some text.
     */
    void text(String string) {
        stringBuilder.append(string)
    }

    /**
     * Add a separator if not already present and it's not the start of the sentence.
     * @param sep The separator, e.g. ' ' or ', '.
     */
    void separator(String sep) {
        if (stringBuilder.length() > 0 && !stringBuilder.toString().endsWith(sep)) {
            stringBuilder.append(sep)
        }
    }

    /**
     * Add a list like 'egg, bacon and ham'.
     * @param conjunction Conjunction between the last two elements, e.g. 'and'.
     * @param elements A list of the elements, e.g. ['egg', 'bacon', 'ham'].
     */
    void list(String conjunction, List<String> elements) {
        switch (elements.size) {
            case 0:
                break

            case 1:
                stringBuilder.append(elements[0])
                break

            default:
                stringBuilder.with {
                    append elements.take(elements.size() - 1).join(', ')
                    separator ' '
                    append conjunction
                    separator ' '
                    append elements.last()
                }
                break
        }
    }

}
