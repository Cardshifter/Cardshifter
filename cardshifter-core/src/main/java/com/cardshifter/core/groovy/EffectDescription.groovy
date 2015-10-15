package com.cardshifter.core.groovy

/**
 * @param text Should be cased and punctuated as if in the middle of a sentence. This makes it possible to later change
 * the ordering of description components. It's easier to capitalize and add punctuation later than the reverse.
 */
enum Trigger {
    AFTER_PLAY(text: ''),
    START_OF_YOUR_TURN(text: 'at the start of your turn'),
    START_OF_OPPONENTS_TURN(text: 'at the start of the opponent\'s turn'),
    START_OF_ANY_TURN(text: 'at the start of a turn'),
    END_OF_YOUR_TURN(text: 'at the end of your turn'),
    END_OF_OPPONENTS_TURN(text: 'at the end of the opponent\'s turn'),
    END_OF_ANY_TURN(text: 'at the end of a turn')

    String text
}

class EffectDescription {

    Trigger trigger = Trigger.AFTER_PLAY

    private StringBuilder builder

    public EffectDescription() {
        builder = new StringBuilder()
    }

    public String toString() {
        StringBuilder descr = new StringBuilder()

        def built = builder.toString()
        if (descr.length() == 0) {
            built = built.capitalize()
        }
        descr.append(built)

        descr.append(trigger.text)
    }

    public void append(String string) {
        builder.append(string);
    }

}
