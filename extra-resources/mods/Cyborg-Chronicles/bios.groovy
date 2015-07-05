card('Conscript') {
    creature "Bio"
    flavor "He just signed up last week and he is very excited to fight."
    attack 2
    health 2
    sickness 0 // rush
    manaCost 2
    // deal one damage to opponent when played
    afterPlay {
        damage 1 on 'opponent'
    }
}
card('Longshot') {
    creature "Bio"
    flavor "Eyes and reflexes augmented for maximum deadliness."
    attack 3
    health 1
    denyCounterAttack 1
    manaCost 3
}
card('Bodyman') {
    creature "Bio"
    flavor "Strength augmented with mechanical musculature."
    attack 2
    health 3
    manaCost 3
}
card('Vetter') {
    creature "Bio"
    flavor "A retired conscript with a desire to jack and make some quick creds."
    health 3
    manaCost 5
    attack 3
    // gain 1 scrap when played
    afterPlay {
        change SCRAP by 1 on 'you'
    }
}
card('Field Medic') {
    creature "Bio"
    flavor "Unsung hero responsible for keeping countless troops alive."
    attack 1
    health 5
    manaCost 5
    // +1 health to owner player after each turn
    onEndOfTurn {
        heal 1 on 'you'
    }
}
card('Wastelander') {
    creature "Bio"
    flavor "Spent his life learning the lessons of the wastelands."
    attack 4
    health 4
    manaCost 6
    // reduce enemy Bio attack by 1 while in play
    whilePresent {
        change ATTACK by -1 withPriority 1 on {
            creatureType 'Bio'
            ownedBy 'opponent'
            zone 'Battlefield'
        }
    }
}
card('Commander') {
    creature "Bio"
    flavor "A professional soldier for the government."
    attack 5
    health 3
    manaCost 6
    sickness 0 // rush
    // on play deal 1 damage to all enemy creatures
    afterPlay {
        change HEALTH by -1 on {
            creature true
            ownedBy 'opponent'
            zone 'Battlefield'
        }
    }
}
card('Cyberpimp') {
    creature "Bio"
    flavor "Supersized and heavily augmented."
    attack 3
    health 5
    manaCost 6
}
card('Cyborg') {
    creature "Bio"
    health 5
    manaCost 7
    attack 5
    flavor "He’s more machine than human now."
}
card('Web Boss') {
    creature "Bio"
    flavor "Leader of a gang that primarily operates on the web."
    attack 6
    health 6
    manaCost 8
    // +1/+1 to all owner's Mechs
    whilePresent {
        change ATTACK, HEALTH by 1 withPriority 1 on {
            creatureType 'Mech'
            ownedBy 'you'
            zone 'Battlefield'
        }
    }

}
card('Inside Man') {
    creature "Bio"
    flavor "A government official with wider web control. Usually brings friends."
    attack 2
    health 6
    noAttack()
    // summon 2 of 3 creatures at random
    afterPlay {
        pick 2 atRandom (
            { summon 1 of 'Bodyman' to 'you' zone 'Battlefield' },
            { summon 1 of 'Conscript' to 'you' zone 'Battlefield' },
            { summon 1 of 'Longshot' to 'you' zone 'Battlefield' }
        )
    }
    manaCost 9
}
