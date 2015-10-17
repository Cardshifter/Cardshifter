package com.cardshifter.core.groovy

class EffectDescription {

    /**
     * The description (value of map) should be cased and punctuated as if in the middle of a sentence. This makes it
     * possible to later change the ordering of description components. It's easier to capitalize and add punctuation
     * later than the reverse. A map is useful instead of enum et al because it's runtime extensible. */
    static Map<String, String> triggerDescription = new HashMap()

    String triggerId
    private StringBuilder builder = new StringBuilder()

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
        SentenceBuilder.build {
            text builder.toString()
            separator ' '
            text triggerDescription.getOrDefault(triggerId, '')
        }
    }

    public void append(String string) {
        builder.append(string);
    }

}
