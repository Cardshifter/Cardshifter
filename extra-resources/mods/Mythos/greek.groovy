package mythos
/* See modding documentation for all available keywords. */
/* GENERIC TEMPLATE
card('Change Me') {
    creature "Greek"
    flavor "hello"
    health 1
    attack 1
    manaCost 1
    sickness 1
}
*/

// CREATURES

card('ZEUS') {
    creature "Greek God"
    flavor "God of the sky, ruler of Mount Olympus."
    health 8
    attack 8
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
    health 8
    attack 8
    manaCost 30
    sickness 1
    // plague()
    // Add +1/+1 to all Chthonic Deities on the field.
    whilePresent {
        change ATTACK, HEALTH by 1 priority 1 onCards {
            creatureType "Greek Chthonic God"
            zone "Battlefield"
            owner "all"
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
    health 8
    attack 4
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



// TOKENS

card('Lost Soul') {
    creature "Greek"
    flavor ""
    health 1
    attack 1
    manaCost 0
    sickness 1
    token()
}
card('Titan 2/2') {
    creature "Greek Titan"
    flavor ""
    health 2
    attack 2
    manaCost 0
    sickness 1
    token()
}
card('Titan 4/2') {
    creature "Greek Titan"
    flavor ""
    health 2
    attack 4
    manaCost 0
    sickness 1
    token()
}
