package com.cardshifter.core.groovy

class EffectDescription {

    /**
     * The description (value of map) should be cased and punctuated as if in the middle of a sentence. This makes it
     * possible to later change the ordering of description components. It's easier to capitalize and add punctuation
     * later than the reverse. A map is useful instead of enum et al because it's runtime extensible. */
    static Map triggerDescription = new HashMap<String, String>()
    String triggerId

    private StringBuilder builder

    public EffectDescription() {
        builder = new StringBuilder()
    }

    public static setupStandardTriggers() {
        triggerDescription.putAll([
                afterPlay: '',
                startOfYourTurn: 'at the start of your turn',
                startOfOpponentsTurn: 'at the start of the opponent\'s turn',
                startOfAnyTurn: 'at the start of a turn',
                endOfYourTurn: 'at the end of your turn',
                endOfOpponentsTurn: 'at the end of the opponent\'s turn',
                endOfAnyTurn: 'at the end of a turn'
        ])
    }

    public String toString() {
        StringBuilder descr = new StringBuilder()

        def built = builder.toString()
        if (descr.length() == 0) {
            built = built.capitalize()
        }
        descr.append(built)

        String triggerText = triggerDescription.getOrDefault(triggerId, '')
        if (descr.length() > 0 && triggerText.length() > 0) {
            descr.append(' ')
        }
        descr.append(triggerText)

        if (descr.length() > 0 && !descr.toString().endsWith('.')) {
            descr.append('.')
        }

        return descr.toString()
    }

    public void append(String string) {
        builder.append(string);
    }

}
