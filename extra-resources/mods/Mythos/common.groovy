package mythos
/* See modding documentation for all available keywords. */
/* GENERIC TEMPLATE
card("ChangeMe") {
    creature "Common"
    flavor "hello"
    health 1
    sickness 1
    manaCost 1
    attack 1
}
*/

/**
 * List of available common cards Cardshifter "Mythos" mod
 * @author www.github.com/Phrancis
 */

card("Swordsman") {
    creature "Common"
    flavor "Armed with a sharp sword."
    maxInDeck 5
    health 3
    sickness 1
    manaCost 5
    attack 3
}
card("Pikeman") {
    creature "Common"
    flavor "Armed with a long armor-piercing pike."
    maxInDeck 5
    health 2
    sickness 1
    manaCost 5
    attack 4
}
card("Archer") {
    creature "Common"
    flavor "Fires arrows from a distance."
    maxInDeck 5
    health 2
    sickness 1
    manaCost 5
    attack 3
    denyCounterAttack()
}
card("Longbowman") {
    creature "Common"
    flavor "Fires devastating arrows from a very long distance."
    health 2
    sickness 1
    manaCost 8
    attack 5
}
card("Defender") {
    creature "Common"
    flavor "Wields a large shield to protect troops."
    health 6
    sickness 1
    manaCost 10
    attack 1
    noAttack()
    taunt()
}
card("Assassin") {
    creature "Common"
    flavor "Strikes with speed and stealth."
    health 2
    sickness 0
    manaCost 10
    attack 6
    denyCounterAttack()
}
card("Spy") {
    creature "Common"
    flavor "Weakens the enemy by revealing their secrets."
    maxInDeck 2
    health 4
    sickness 1
    manaCost 10
    attack 0
    noAttack()
    whilePresent {
        change HEALTH, ATTACK by -1 withPriority 1 onCards {
            creature true
            ownedBy 'opponent'
            zone 'Battlefield'
        }
    }
}
card("Slingman") {
    creature "Common"
    flavor "Slings stones to break enemy weapons."
    maxInDeck 2
    health 3
    sickness 1
    manaCost 10
    attack 4
    whilePresent {
        change ATTACK by -2 withPriority 1 onCards {
            creature true
            ownedBy 'opponent'
            zone 'Battlefield'
        }
    }
}
card("Healer") {
    creature "Common"
    flavor "Supports troops by providing healing."
    maxInDeck 2
    health 5
    sickness 1
    manaCost 10
    attack 0
    noAttack()
    whilePresent {
        change HEALTH by 2 withPriority 1 onCards {
            creature true
            ownedBy 'you'
            zone 'Battlefield'
        }
    }
}
