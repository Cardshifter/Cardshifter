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
            creature() // all creatures
            zone 'Battlefield'
        }
    }
}
card('DIYU') {
    creature "Chinese"
    flavor "The Realm of the Dead, containing the Ten Courts of Hell."
    // Type: Place
    // Info: http://en.wikipedia.org/wiki/Diyu
    // Image: http://upload.wikimedia.org/wikipedia/commons/c/cb/Jade_Record_1.PNG
    // License: Public Domain
    health 10
    sickness 0
    manaCost 20
    attack 0
    noAttack()
    afterPlay {
        summon 1 of 'Yaoguai' to 'you' zone 'Battlefield'
        summon 1 of 'Yaomo' to 'you' zone 'Battlefield'
        summon 1 of 'Yaojing' to 'you' zone 'Battlefield'
    }
    whilePresent {
        change HEALTH by -1 withPriority 1 onCards {
            creature() // all creatures
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
    flavor "Monkey King Warrior of Immense Strength."
    // Type: Person
    // Info: http://en.wikipedia.org/wiki/Sun_Wukong
    // Image: http://upload.wikimedia.org/wikipedia/commons/2/25/Sun_Wukong_and_Jade_Rabbit.jpg
    // License: Public Domain
    health 5
    sickness 0
    manaCost 15
    attack 5
    denyCounterAttack()
    onEndOfTurn {
        change ATTACK by 1 onCards {
            card(this)
        }
    }
}
card('GUAN YU') {
    creature "Chinese"
    flavor "God of War and Martial Arts."
    // Type: Person
    // Info: http://en.wikipedia.org/wiki/Guan_Yu
    // Image: http://upload.wikimedia.org/wikipedia/commons/e/eb/Guan_yu_-Summer_Palace%2C_Beijing.JPG
    // License: CC BY-SA 3.0
    // Author: http://commons.wikimedia.org/wiki/User:Shizhao
    health 10
    sickness 1
    manaCost 15
    attack 5
    afterPlay {
        summon 2 of 'Terracotta Soldier' to 'you' zone 'Hand'
    }
}
card('EIGHT IMMORTALS') {
    creature "Chinese"
    flavor "Their power can be transferred to a tool that can bestow life or destroy evil."
    // Type: Person
    // Info: http://en.wikipedia.org/wiki/Eight_Immortals
    // Image: http://upload.wikimedia.org/wikipedia/commons/9/9a/Eight_Immortals_Crossing_the_Sea_-_Project_Gutenberg_eText_15250.jpg
    // License: Public Domain
    health 5
    sickness 1
    manaCost 10
    attack 3
    afterPlay {
        pickAtRandom(
            { summon 1 of "Life Tool" to 'you' zone 'Battlefield' },
            { summon 1 of "Destruction Tool" to 'you' zone 'Battlefield' }
        )
    }
}
card('SHINJE') {
    creature "Chinese"
    flavor "Wrathful God of Death and Guardian of Spiritual Practice."
    // Type: Person
    // Info: http://en.wikipedia.org/wiki/Yama_(East_Asia)#Yama_in_Tibetan_Buddhism
    // Image: http://upload.wikimedia.org/wikipedia/commons/5/54/Yama_tibet.jpg
    // License: Püblic Domain
    health 6
    sickness 1
    manaCost 20
    attack 6
    whilePresent {
        change ATTACK, HEALTH by 1 withPriority 1 onCards {
            creature()
            ownedBy "you"
            zone "Battlefield"
        }
        change MANA_COST by -3 onCards {
            ownedBy "you"
            zone "Hand", "Battlefield"
        }
    }
}
card('GUANYIN') {
    creature "Chinese"
    flavor "Perceives the Cries of the World"
    // Type: Person
    // Info: http://en.wikipedia.org/wiki/Guanyin
    // Image: http://upload.wikimedia.org/wikipedia/commons/8/89/Daienin_Kannon.JPG
    // License: CC BY-SA 3.0
    // Author: ":...---...SOS" | http://commons.wikimedia.org/w/index.php?title=User:...---...SOS&action=edit&redlink=1
    health 10
    sickness 1
    manaCost 15
    attack 0
    noAttack()
    whilePresent {
        change HEALTH by 1 withPriority 1 onCards {
            creature() 
            ownedBy 'you' 
            zone 'Battlefield'
        }
    }
    onEndOfTurn {
        heal 1 to 'you'
        damage 1 to { card(this) }
    }
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

card('Terracotta Soldier') {
    creature "Chinese"
    flavor "Armies of Qin Shi Huang, the first Emperor of China."
    // Info: http://en.wikipedia.org/wiki/Terracotta_Army
    // Image: http://upload.wikimedia.org/wikipedia/commons/2/22/Officer_Terrakottaarm%C3%A9n.jpg
    // License: CC BY-SA 3.0
    // Author: Tor Svensson | http://sv.wikipedia.org/wiki/User:Kemitsv
    token()
    manaCost 5
    sickness 1
    health 3
    attack 3
    taunt()
}

card('Yaoguai') {
    creature "Chinese"
    flavor "Freak"
    // Info: http://en.wikipedia.org/wiki/Yaoguai
    token()
    sickness 1
    manaCost 0
    health 2
    attack 2
    denyCounterAttack()
}
card('Yaomo') {
    creature "Chinese"
    flavor "Demon"
    // Info: http://en.wikipedia.org/wiki/Yaoguai
    token()
    sickness 1
    manaCost 0
    health 2
    attack 3
}
card('Yaojing') {
    creature "Chinese"
    flavor "Pixie"
    // Info: http://en.wikipedia.org/wiki/Yaoguai
    token()
    sickness 1
    manaCost 0
    health 3
    attack 0
    noAttack()
    taunt()
}
card("Life Tool") {
    creature "Chinese"
    flavor "Restores life."
    token()
    health 3
    attack 0
    noAttack()
    onEndOfTurn {
        heal 1 to 'you'
    }
}
card("Destruction Tool") {
    creature "Chinese"
    flavor "Causes damage."
    token()
    health 3
    attack 0
    noAttack()
    onEndOfTurn {
        damage 1 to 'opponent'
    }
}
