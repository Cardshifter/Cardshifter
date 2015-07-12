package mythos
/**
 * The Hindu cardset uses many variants of random and probabilities, to create a lively
 * and unpredictable strategy that often benefits the player, but sometimes the opponent.
 * @author https://github.com/Phrancis
 * Created 2015-07-12
 */

card('INDRA') {
    creature 'Hindu God'
    flavor 'The one who rides the Clouds, the Lord of the Gods and of Heaven.'
    // https://en.wikipedia.org/wiki/Indra
    // https://upload.wikimedia.org/wikipedia/commons/3/36/Indra_deva.jpg
    // Public domain
    maxInDeck 1
    attack 5
    health 8
    sickness 1
    manaCost 15
    afterPlay {
        summon 1 of 'Airavata' to 'you' zone 'Hand'
    }
    // give Rush to all creatures on Battlefield, counters "freeze" type effects
    whilePresent {
        set SICKNESS to 0 withPriority 1 on {
            creature true
            zone 'Battlefield'
        }
    }
}
// INDRA token
card('Airavata') {
    creature 'Hindu'
    flavor 'Indra\'s sacred three-headed white elephant.'
    // https://en.wikipedia.org/wiki/Airavata
    // https://upload.wikimedia.org/wikipedia/commons/4/4e/Indradeva.jpg
    // Public domain
    token()
    attack 2
    health 8
    sickness 1
    manaCost 10
    whilePresent {
        change ATTACK, HEALTH by 3 withPriority 1 on {
            cardName 'INDRA'
            ownedBy 'you'
            zone 'Battlefield'
        }
    }
}

card('BRAHMA') {
    creature 'Hindu God'
    flavor 'Creator of the Universe, grand-father of all humans.'
    // https://en.wikipedia.org/wiki/Brahma
    // https://upload.wikimedia.org/wikipedia/commons/e/e4/Brahma_on_hamsa.jpg
    // Public Domain
    maxInDeck 1
    attack 5
    health 6
    sickness 1
    manaCost 15
    afterPlay {
        summon 1 of 'Hansa' to 'you' zone 'Hand'
    }
    // give +2/+2 to Common creatures
    whilePresent {
        change ATTACK, HEALTH by 2 withPriority 1 on {
            creatureType 'Common'
            zone 'Battlefield'
        }
    }
}
// BRAHMA TOKEN
card('Hansa') {
    creature 'Hindu'
    flavor 'Brahma\'s white swan mount.'
    // https://en.wikipedia.org/wiki/Hamsa_(bird)
    token()
    attack 2
    health 5
    manaCost 5
    whilePresent {
        change HEALTH by 2 withPriority 1 on {
            cardName 'BRAHMA'
            ownedBy 'you'
            zone 'Battlefield'
        }
    }
}


card('VISHNU') {
    creature 'Hindu God'
    flavor 'The All-Pervading One. "Whatever that is there is the world of change."'
    // https://en.wikipedia.org/wiki/Vishnu
    // https://upload.wikimedia.org/wikipedia/commons/c/c2/Bhagavan_Vishnu.jpg
    // Public Domain
    maxInDeck 1
    attack 5
    health 5
    sickness 1
    manaCost 10
    /*  // Issue #326
    onEndOfTurn {
        pick 1 atRandom (
                { change HEALTH by 1  on { thisCard() } },
                { change ATTACK by 1  on { thisCard() } },
                { change HEALTH by -1 on { thisCard() } },
                { change ATTACK by 2  on { thisCard() } },
                { set SICKNESS to 2   on { thisCard() } }
        )
    }*/
    // temporary replacement
    onEndOfTurn {
        pick 1 atRandom (
                { heal 1    on { thisCard() } },
                { heal 2    on { thisCard() } },
                { damage 1  on { thisCard() } },
                { damage 1  on 1 random { creature true; ownedBy 'opponent'; zone 'Battlefield' } }
        )
    }
}

card('KRISHNA') {
    creature 'Hindu God'
    flavor 'Eighth incarnation of Lord Vishnu. Handsome blue-skinned Hero God.'
    // https://en.wikipedia.org/wiki/Krishna
    // https://upload.wikimedia.org/wikipedia/commons/7/70/Krishna_Holding_Mount_Govardhan_-_Crop.jpg
    // Public Domain
    maxInDeck 1
    attack 5
    health 7
    sickness 0 // rush
    manaCost 15
    whilePresent {
        change ATTACK, HEALTH by 1 withPriority 1 on {
            creature true
            ownedBy 'you'
            zone 'Battlefield'
        }
    }
}

card('SHIVA') {
    creature 'Hindu God'
    flavor 'The Auspicious One, He has many Benevolent and Fearsome forms.'
    // https://en.wikipedia.org/wiki/Shiva
    // https://upload.wikimedia.org/wikipedia/commons/a/a7/6_%C5%9Aiva_and_P%C4%81rvat%C4%AB_seated_on_a_terrace._1800_%28circa%29_BM.jpg
    // Public Domain
    maxInDeck 1
    attack 4
    health 5
    sickness 1
    manaCost 10
    afterPlay {
        pick 1 atRandom (
                { damage 3  on { creature true; ownedBy 'opponent'; zone 'Battlefield' } },
                { heal 3    on { creature true; ownedBy      'you'; zone 'Battlefield' } }
        )
    }
}

card('YAMA') {
    creature 'Hindu God'
    flavor 'The first mortal who died, now the Ruler of the Departed.'
    // https://en.wikipedia.org/wiki/Yama
    // https://upload.wikimedia.org/wikipedia/commons/3/33/Yama_on_buffalo.jpg
    // Public Domain
    maxInDeck 1
    attack 4
    health 6
    sickness 1
    manaCost 10
    onDeath { withProbability(0.7) { heal 2    on { creature true; ownedBy 'you'; zone 'Battlefield' } } }
    onDeath { withProbability(0.7) { damage 2  on { creature true; ownedBy 'opponent'; zone 'Battlefield' } } }
}


card('DURGA') {
    creature 'Hindu God'
    flavor 'Goddess armed with Celestial Weapons of all Deities.'
    // https://en.wikipedia.org/wiki/Durga
    // https://upload.wikimedia.org/wikipedia/commons/f/f5/Durga_Mahisasuramardini.JPG
    // Public Domain
    maxInDeck 1
    attack 7
    health 4
    sickness 1
    manaCost 15
    afterPlay {
        repeat(2) {
            damage 1 on 1 random { creature true; ownedBy 'opponent'; zone 'Battlefield' }
        }
    }
    onStartOfTurn {
        withProbability(0.5) {
            damage 1 on 1 random { creature true; ownedBy 'opponent'; zone 'Battlefield' }
        }
    }
}

card('VARUNA') {
    creature 'Hindu God'
    flavor 'God of the Oceans and of Law and Morals.'
    // https://en.wikipedia.org/wiki/Varuna
    // https://en.wikipedia.org/wiki/Varuna#/media/File:Varunadeva.jpg
    // Public Domain
    maxInDeck 1
    attack 3
    health 6
    sickness 1
    manaCost 10
    onStartOfTurn {
        heal 1 on 2 random { creature true; zone 'Battlefield' }
    }
    whilePresent {
        change HEALTH by 1 withPriority 1 on { creature true; ownedBy 'you'; zone 'Battlefield' }
    }
}

card('Gautama Buddha') {
    creature 'Hindu Hero'
    flavor 'The Enlightened One, discoverer of the Middle Way.'
    // https://en.wikipedia.org/wiki/Gautama_Buddha
    // https://upload.wikimedia.org/wikipedia/commons/c/c1/Astasahasrika_Prajnaparamita_Victory_Over_Mara.jpeg
    // Public Domain
    attack 4
    health 6
    sickness 1
    manaCost 10
    onEndOfTurn {
        pick 1 atRandom (
                { heal 1    on 'you' },
                { heal 1    on 1 random { creature true; zone 'Battlefield' } },
                { damage 1  on 'opponent' },
                { damage 1  on 1 random { creature true; zone 'Battlefield' } }
        )
    }
}

card('Maitreya Buddha') {
    creature 'Hindu Hero'
    flavor 'Successor of Prophecy of the Buddha, to achieve Complete Enlightenment and teach the Pure Dharma'
    // https://en.wikipedia.org/wiki/Maitreya
    // https://upload.wikimedia.org/wikipedia/commons/2/2d/Maitreya_Buddha%2C_Nubra.jpg
    // CC BY-SA 3.0
    // https://commons.wikimedia.org/wiki/User:John%20Hill
    maxInDeck 1
    attack 3
    health 9
    sickness 1
    manaCost 20
    whilePresent {
        change HEALTH by 2 withPriority 1 on { creature true; ownedBy 'you'; zone 'Battlefield' }
    }
    onEndOfTurn {
        withProbability(0.75) {
            pick 1 atRandom (
                    { heal 1 on    'you' },
                    { damage 1 on  'opponent' }
            )
        }
    }
}