/* See modding documentation for all available keywords. */
/* GENERIC TEMPLATE
card('ChangeMe') {
    creature "Chinese"
    flavor "hello"
    health 1
    sickness 1
    manaCost 1
    attack 1
}
*/

/**
 * List of available cards for Chinese panthon for Cardshifter "Mythos" mod
 * @author www.github.com/Phrancis
 */

card('JADE EMPEROR') {
    creature "Chinese"
    flavor "The Great Grandfather, Emperor of all Deities, Vanquisher of Evil."
    // Type: Person
    // Info: http://en.wikipedia.org/wiki/Jade_Emperor
    // Image: http://upload.wikimedia.org/wikipedia/commons/2/24/Jade_Emperor._Ming_Dynasty.jpg
    // License: Public Domain
    health 10
    sickness 1
    manaCost 30
    attack 3
    whilePresent {
        change HEALTH by 3 withPriority 1 onCards {
            creatureType 'Chinese'
            ownedBy 'you'
            zone 'Battlefield'
        }
        change ATTACK by -1 withPriority 2 onCards {
            creature()
            zone 'Battlefield'
        }
    }
}
card('DIYU') {
    creature "Chinese"
    flavor "The Realm of the Dead, containing the The Courts of Hell."
    // Type: Place
    // Info: http://en.wikipedia.org/wiki/Diyu
    // Image: http://upload.wikimedia.org/wikipedia/commons/c/cb/Jade_Record_1.PNG
    // License: Public Domain
    health 10
    sickness 0
    manaCost 20
    attack 0
    noAttack true
    afterPlay {
        summon 1 of 'Yaoguai' to 'you' zone 'Battlefield'
        summon 1 of 'Yaomo' to 'you' zone 'Battlefield'
        summon 1 of 'Yaojing' to 'you' zone 'Battlefield'
    }
    whilePresent {
        change HEALTH by -1 withPriority 1 onCards {
            creature()
            zone 'Battlefield'
        }
        change ATTACK by 1 withPriority 2 onCards {
            creatureType 'Chinese'
            ownedBy 'you'
            zone 'Battlefield'
        }
    }
}
card('MONKING') {
    creature "Chinese"
    flavor "The infamous irrepressible Monkey King and Great Sage Equal Of Heaven"
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('GUAN-YU') {
    creature "Chinese"
    flavor "God of War and Martial Arts"
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('EIGHT IMMORTALS') {
    creature "Chinese"
    flavor "Accidental immortals, gods and goddesses of perfection."
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('YEN-LO-WANG') {
    creature "Chinese"
    flavor "Deity of Death and Lord of the Fifth Floor of Hell"
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('GUAN-YIN') {
    creature "Chinese"
    flavor "Goddess of Compassion and Caring"
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('AO-CHIN') {
    creature "Chinese"
    flavor "Sea Spirit. King of the Southern Ocean"
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('NU-GUA') {
    creature "Chinese"
    flavor "Serpent Creator Goddess of Mankind, Marriage and Mud"
    health 1
    sickness 1
    manaCost 1
    attack 1
}
card('CAO-GUOJIU') {
    creature "Chinese"
    flavor "Immortal theatre fan and royal patron of actors"
    health 1
    sickness 1
    manaCost 1
    attack 1
}

// TOKENS 
card('Yaoguai') {
    creature "Chinese"
    flavor "Freak"
    token true
    sickness 1
    manaCost 0
    health 2
    attack 2
    denyCounterAttack true
}
card('Yaomo') {
    creature "Chinese"
    flavor "Demon"
    token true
    sickness 1
    manaCost 0
    health 2
    attack 3
}
card('Yaojing') {
    creature "Chinese"
    flavor "Pixie"
    token true
    sickness 1
    manaCost 0
    health 3
    attack 0
    noAttack true
    taunt true
}
