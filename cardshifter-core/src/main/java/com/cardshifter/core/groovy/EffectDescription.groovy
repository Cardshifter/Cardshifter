package com.cardshifter.core.groovy

class EffectDescription {

    /**
     * The description (value of map) should be cased and punctuated as if in the middle of a sentence. This makes it
     * possible to later change the ordering of description components. It's easier to capitalize and add punctuation
     * later than the reverse. A map is useful instead of enum et al because it's runtime extensible. */
    static Map<String, String> vocabulary = new HashMap()

    static {
        setupStandardVocabulary()
    }

    // Key into vocabulary
    String triggerId

    // choose $randomChoiceCount atRandom { $randomChoices... }
    int randomChoiceCount
    List<EffectDescription> randomChoices

    private StringBuilder builder = new StringBuilder()

    private static void setupStandardVocabulary() {
        vocabulary.putAll([
                afterPlay: '',
                onStartOfYourTurn: 'at the start of your turn',
                onStartOfOpponentsTurn: 'at the start of the opponent\'s turn',
                onStartOfAnyTurn: 'at the start of a turn',
                onEndOfYourTurn: 'at the end of your turn',
                onEndOfOpponentsTurn: 'at the end of the opponent\'s turn',
                onEndOfAnyTurn: 'at the end of a turn',
                onDeath: 'when this dies',
                pickAtRandom: 'choose %count% at random'
        ])
    }

    public String toString() {
        SentenceBuilder.build {
            text vocabulary.getOrDefault(triggerId, '')
            separator ', '

            text builder.toString()
            separator ' '

            if (randomChoiceCount > 0) {
                separator 'and '
                text vocabulary.getOrDefault('pickAtRandom', '').replace('%count%', "$randomChoiceCount")
                separator ': '
                def beautify = { '"' + (it.endsWith('.') ? it.substring(0, it.length() - 1) : it) + '"' }
                list 'or', randomChoices*.toString().collect(beautify)
            }
        }
    }

    public void append(String string) {
        builder.append(string);
    }

}
