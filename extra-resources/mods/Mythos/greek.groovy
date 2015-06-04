package mythos
/* See modding documentation for all available keywords. */
/* GENERIC TEMPLATE
card('ChangeMe') {
    creature "Greek"
    flavor "hello"
    attack 1
    health 1
    manaCost 1
    sickness 1
}
*/

// CREATURES

card('ZEUS') {
    creature "Greek God"
    flavor "God of the sky, ruler of Mount Olympus."
    attack 8
    health 8
    manaCost 30
    sickness 1
    denyCounterAttack() // ranged
    /*
    pickAction {
        // (3) Deal +1 damage to target creature or player per turn.
        { damage 1 to target cost 3 mana }
        // (5) Deal +5 damage to target creature or player per turn.
        { damage 3 to target cost 3 mana }
    }
    */
}

card('HADES') {
    creature "Greek Chthonic God"
    flavor "God of the Underworld."
    attack 8
    health 8
    manaCost 30
    sickness 1
    // plague()
    // Add +1/+1 to all Chthonic Deities on the field.
    whilePresent {
        change ATTACK, HEALTH by 1 withRriority 1 onCards {
            creatureType "Greek Chthonic God"
            zone "Battlefield"
            ownedBy "you"
        }
    }
    // When Hades is sent to the graveyard, add three +1/+1 Lost Soul tokens to the field.
    onDeath {
        summon 3 of "Lost Soul" to "you" zone "Battlefield"
    }
}

card('CRONUS') {
    creature "Greek Titan"
    flavor "Deposed King of the Titans; also father of Zeus, Hades, and Poseidon."
    attack 4
    health 8
    manaCost 30
    sickness 1
    // phase()
    // When this creature is put into play, add one +2/+2 Titan Token to the field.
    afterPlay {
        summon 1 of "Titan 2/2" to "you" zone "Battlefield"
    }
    // When this creature is sent to the graveyard, add one +4/+2 Titan Token to the Field.
    onDeath {
        summon 1 of "Titan 4/2" to "you" zone "Battlefield"
    }
}

card('Heracles') {
    creature "Greek Hero"
    flavor "Ascended hero, Son of Zeus, and divine protector of mankind."
    attack 6
    health 8
    manaCost 25
    sickness 0 // Rush
    // When Heracles comes into play, deal +3 damage randomly to target unit or player.
    afterPlay {
        pick 1 atRandom (
            { damage 3 to "opponent" },
            { damage 3 to 1 random { creature true; owner "opponent" } }
        )
    }
    // Add +1/+1 to all Hero units on the field.
    whilePresent {
        change ATTACK, HEALTH by 1 withPriority 1 onCards {
            creatureType "Greek Hero"
            ownedBy "you"
            zone "Battlefield"
        }
    }
}

card('Lernaean Hydra') {
    creature "Greek"
    flavor "A many-headed, serpent-like creature that guarded an Underworld entrance beneath Lake Lerna."
    attack 6
    health 3
    manaCost 25
    sickness 1
    taunt()
    // When this card is attacked, but not destroyed, it gains +1 attack.
    /*
    afterAttacked {
        change ATTACK by 1 onCards { thisCard }
    }
    */
    // Give all Creature units +1/+1
    whilePresent {
        change ATTACK, HEALTH by 1 withPriority 1 onCards {
            creature true
            ownedBy "you"
            zone "Battlefield"
        }
    }
}

card('The Underworld') {
    creature "Greek Location"
    flavor "The realm of the Dead."
    attack 0
    health 8
    manaCost 20
    sickness 1
    noAttack()
    // Add +2/+2 to all Chthonic Deities on the field.
    whilePresent {
        change ATTACK, HEALTH by 2 withPriority 1 onCards {
            creatureType "Greek Chthonic God"
            ownedBy "you"
            zone "Battlefield"
        }
    }
    // Add +5 attack to this card if Hades is on the field.
    /*
    ifPresent (card "HADES", zone "Battlefield") {
        change ATTACK by 5 withPriority 2 onCards { thisCard() }
    }
    */
}


// TOKENS

card('Lost Soul') {
    creature "Greek"
    flavor ""
    attack 1
    health 1
    manaCost 0
    sickness 1
    token()
}
card('Titan 2/2') {
    creature "Greek Titan"
    flavor ""
    attack 2
    health 2
    manaCost 0
    sickness 1
    token()
}
card('Titan 4/2') {
    creature "Greek Titan"
    flavor ""
    attack 4
    health 2
    manaCost 0
    sickness 1
    token()
}
