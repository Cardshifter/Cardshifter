package mythos
/* See modding documentation for all available keywords. */
/* GENERIC TEMPLATE
card('ChangeMe') {
    creature 'Chinese'
    flavor 'hello'
    health 1
    sickness 1
    manaCost 1
    attack 1
}
*/

/**
 * List of available cards for Chinese panthon for Cardshifter 'Mythos' mod
 * @author www.github.com/Phrancis
 */

// PANTHEON

card('JADE EMPEROR') {
    creature 'Chinese God'
    flavor 'The Great Grandfather, Emperor of all Deities, Vanquisher of Evil.'
    // Info: http://en.wikipedia.org/wiki/Jade_Emperor
    // Image: http://upload.wikimedia.org/wikipedia/commons/2/24/Jade_Emperor._Ming_Dynasty.jpg
    // License: Public Domain
    maxInDeck 1
    health 10
    sickness 1
    manaCost 30
    attack 3
    whilePresent {
        change HEALTH by 3 withPriority 1 on {
            creatureType 'Chinese'
            ownedBy 'you'
            zone 'Battlefield'
        }
        change ATTACK by -1 withPriority 2 on {
            creature true
            zone 'Battlefield'
        }
    }
}
card('NUWA') {
    creature 'Chinese God'
    flavor 'Serpent Creator Goddess of Mankind and Repairer of the Pillar of Heaven.'
    
    // Info: http://en.wikipedia.org/wiki/N%C3%BCwa
    // Image: http://upload.wikimedia.org/wikipedia/commons/4/49/Nuwa.jpg
    // License: Public Domain
    maxInDeck 1
    health 10
    sickness 1
    manaCost 25
    attack 4
    afterPlay {
        summon 1 of 'Terracotta Soldier' to 'you' zone 'Battlefield'
        summon 1 of 'Manchu Archer' to 'you' zone 'Battlefield'
        summon 1 of 'Kung Fu Fighter' to 'you' zone 'Battlefield'
    }
    onEndOfTurn {
        pick 1 atRandom (
            { heal 2 on 'you' },
            { heal 1 on 'opponent' },
            { damage 1 on 'you' },
            { damage 2 on 'opponent' }
        )
    }
}
card('DIYU') {
    creature 'Chinese Place'
    flavor 'The Realm of the Dead, containing the Ten Courts of Hell.'
    
    // Info: http://en.wikipedia.org/wiki/Diyu
    // Image: http://upload.wikimedia.org/wikipedia/commons/c/cb/Jade_Record_1.PNG
    // License: Public Domain
    maxInDeck 1
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
        change HEALTH by -1 withPriority 1 on {
            creature true // all creatures
            zone 'Battlefield'
        }
        change ATTACK by 1 withPriority 2 on {
            creatureType 'Chinese'
            ownedBy 'you'
            zone 'Battlefield'
        }
    }
}
// Tokens for DIYU
card('Yaoguai') {
    creature 'Chinese'
    flavor 'Freak'
    // Info: http://en.wikipedia.org/wiki/Yaoguai
    token()
    sickness 1
    manaCost 0
    health 2
    attack 2
    denyCounterAttack()
}
card('Yaomo') {
    creature 'Chinese'
    flavor 'Demon'
    // Info: http://en.wikipedia.org/wiki/Yaoguai
    token()
    sickness 1
    manaCost 0
    health 2
    attack 3
}
card('Yaojing') {
    creature 'Chinese'
    flavor 'Pixie'
    // Info: http://en.wikipedia.org/wiki/Yaoguai
    token()
    sickness 1
    manaCost 0
    health 3
    attack 0
    noAttack()
    taunt()
}

card('MONKING') {
    creature 'Chinese Hero'
    flavor 'Monkey King Warrior of Immense Strength.'
    
    // Info: http://en.wikipedia.org/wiki/Sun_Wukong
    // Image: http://upload.wikimedia.org/wikipedia/commons/2/25/Sun_Wukong_and_Jade_Rabbit.jpg
    // License: Public Domain
    maxInDeck 2
    health 5
    sickness 0
    manaCost 15
    attack 5
    denyCounterAttack()
    onEndOfTurn {
        change ATTACK by 1 on {
            thisCard()
        }
    }
}
card('GUAN YU') {
    creature 'Chinese God'
    flavor 'God of War and Martial Arts.'
    
    // Info: http://en.wikipedia.org/wiki/Guan_Yu
    // Image: http://upload.wikimedia.org/wikipedia/commons/e/eb/Guan_yu_-Summer_Palace%2C_Beijing.JPG
    // License: CC BY-SA 3.0
    // Author: http://commons.wikimedia.org/wiki/User:Shizhao
    maxInDeck 2
    health 10
    sickness 1
    manaCost 15
    attack 5
    afterPlay {
        summon 2 of 'Terracotta Soldier' to 'you' zone 'Hand'
    }
}
card('EIGHT IMMORTALS') {
    creature 'Chinese'
    flavor 'Their power can be transferred to a tool that can bestow life or destroy evil.'
    
    // Info: http://en.wikipedia.org/wiki/Eight_Immortals
    // Image: http://upload.wikimedia.org/wikipedia/commons/9/9a/Eight_Immortals_Crossing_the_Sea_-_Project_Gutenberg_eText_15250.jpg
    // License: Public Domain
    health 5
    sickness 1
    manaCost 10
    attack 3
    afterPlay {
        pick 1 atRandom (
            { summon 1 of 'Life Tool' to 'you' zone 'Battlefield' },
            { summon 1 of 'Destruction Tool' to 'you' zone 'Battlefield' }
        )
    }
}
// Tokens for EIGHT IMMORTALS
card('Life Tool') {
    creature 'Chinese'
    flavor 'Restores life.'
    token()
    health 3
    attack 0
    noAttack()
    onEndOfTurn {
        heal 1 on 'you'
    }
}
card('Destruction Tool') {
    creature 'Chinese'
    flavor 'Causes damage.'
    token()
    health 3
    attack 0
    noAttack()
    onEndOfTurn {
        damage 1 on 'opponent'
    }
}

card('SHINJE') {
    creature 'Chinese God'
    flavor 'Wrathful God of Death and Guardian of Spiritual Practice.'
    
    // Info: http://en.wikipedia.org/wiki/Yama_(East_Asia)#Yama_in_Tibetan_Buddhism
    // Image: http://upload.wikimedia.org/wikipedia/commons/5/54/Yama_tibet.jpg
    // License: Püblic Domain
    maxInDeck 1
    health 6
    sickness 1
    manaCost 20
    attack 6
    whilePresent {
        change ATTACK, HEALTH by 1 withPriority 1 on {
            creature true
            ownedBy 'you'
            zone 'Battlefield'
        }
        change MANA_COST by -3 withPriority 1 on {
            ownedBy 'you'
            zone 'Hand', 'Battlefield'
        }
    }
}
card('GUANYIN') {
    creature 'Chinese God'
    flavor 'Perceives the Cries of the World'
    
    // Info: http://en.wikipedia.org/wiki/Guanyin
    // Image: http://upload.wikimedia.org/wikipedia/commons/8/89/Daienin_Kannon.JPG
    // License: CC BY-SA 3.0
    // Author: ':...---...SOS' | http://commons.wikimedia.org/w/index.php?title=User:...---...SOS&action=edit&redlink=1
    maxInDeck 2
    health 10
    sickness 1
    manaCost 15
    attack 0
    noAttack()
    whilePresent {
        change HEALTH by 1 withPriority 1 on {
            creature true
            ownedBy 'you'
            zone 'Battlefield'
        }
    }
    onEndOfTurn {
        heal 1 on 'you'
        change HEALTH by -1 on { thisCard() }
    }
}
card('MAZU') {
    creature 'Chinese God'
    flavor 'Silent Goddess watching over Seafarers.'
    
    // Info: http://en.wikipedia.org/wiki/Mazu_(goddess)
    // Image: http://upload.wikimedia.org/wikipedia/commons/c/cf/Mazu_statue.JPG
    // License: CC BY-SA 3.0
    // Author: http://commons.wikimedia.org/wiki/User:Dli184
    health 8
    sickness 1
    manaCost 10
    attack 3
    whilePresent {
        change HEALTH by 1 withPriority 1 on {
            creature true
            ownedBy 'you'
            zone 'Battlefield'
        }
        change HEALTH by 2 withPriority 2 on {
            creature true
            ownedBy 'active'
            zone 'Battlefield'
        }
    }
}

// COMMON UNITS

card('Terracotta Soldier') {
    creature 'Chinese'
    flavor 'Armies of Qin Shi Huang, the first Emperor of China.'
    
    // Info: http://en.wikipedia.org/wiki/Terracotta_Army
    // Image: http://upload.wikimedia.org/wikipedia/commons/2/22/Officer_Terrakottaarm%C3%A9n.jpg
    // License: CC BY-SA 3.0
    // Author: Tor Svensson | http://sv.wikipedia.org/wiki/User:Kemitsv
    manaCost 5
    sickness 1
    health 3
    attack 3
    taunt()
}
card('Manchu Archer') {
    creature 'Chinese'
    flavor: 'Archers have played a pivotal role in Chinese society.'
    
    // Info: http://en.wikipedia.org/wiki/Chinese_archery
    // Image: http://upload.wikimedia.org/wikipedia/commons/2/20/Manchuguard.jpg
    // License: Public Domain
    manaCost 5
    sickness 1
    health 4
    attack 2
    denyCounterAttack()
}
card('Kung Fu Fighter') {
    creature 'Chinese'
    flavor: 'Chinese Martial Artist.'
    
    // Info: http://en.wikipedia.org/wiki/Chinese_martial_arts
    // Image: https://upload.wikimedia.org/wikipedia/commons/0/0b/Beddar.jpg
    // License: CC-BY-SA-3.0
    // Author: Scrozentalis | https://commons.wikimedia.org/w/index.php?title=User:Srozentalis&action=edit&redlink=1
    manaCost 5
    sickness 0
    health 3
    attack 4
}

// TOKENS



