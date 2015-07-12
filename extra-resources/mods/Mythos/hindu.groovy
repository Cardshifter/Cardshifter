package mythos
/* See modding documentation for all available keywords. */
/* GENERIC TEMPLATE
card('ChangeMe') {
    creature 'Hindu'
    flavor 'hello'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
*/

card('INDRA') {
    creature 'Hindu God'
    flavor 'The one who rides the Clouds, the Lord of the Gods and of Heaven.'
    // https://en.wikipedia.org/wiki/Indra
    // https://upload.wikimedia.org/wikipedia/commons/3/36/Indra_deva.jpg
    // Public domain
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

/*
card('VISHNU') {
    creature 'Hindu'
    flavor 'Shape-changing Preserver of the Universe'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('KRISHNA') {
    creature 'Hindu'
    flavor 'Popular and Handsome blue-skinned Hero God'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('BUDDHA') {
    creature 'Hindu'
    flavor 'God of Wisdom and Enlightened One'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('SHIVA') {
    creature 'Hindu'
    flavor 'Dancing God of Universal Destruction'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('YAMA') {
    creature 'Hindu'
    flavor 'Buffalo-headed God of Death'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('DURGA') {
    creature 'Hindu'
    flavor 'Demon-killing Warrior Goddess'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('ChangeMe') {
    creature 'Hindu'
    flavor 'hello'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('VARUNA') {
    creature 'Hindu'
    flavor 'Sky God of Law and Order'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('MAITREYA') {
    creature 'Hindu'
    flavor 'Happiness God and Buddha of the Future'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
*/