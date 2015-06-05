package mythos
/* See modding documentation for all available keywords. */
/* GENERIC TEMPLATE
card('ChangeMe') {
    creature "Greek"
    flavor "hello"
    maxInDeck 1
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
    maxInDeck 1
    attack 8
    health 8
    manaCost 30
    sickness 1
    denyCounterAttack() // ranged
    /*
    pickAction (
        // (3) Deal +1 damage to target creature or player per turn.
        { damage 1 to target cost 3 mana }
        // (5) Deal +5 damage to target creature or player per turn.
        { damage 3 to target cost 3 mana }
    )
    */
}

// CHTHONIC DEITIES

card('HADES') {
    creature "Greek Chthonic Deity"
    flavor "God of the Underworld."
    maxInDeck 1
    attack 8
    health 8
    manaCost 30
    sickness 1
    // plague()
    // Add +1/+1 to all Chthonic Deities on the field.
    whilePresent {
        change ATTACK, HEALTH by 1 withPriority 1 onCards {
            creatureType "Greek Chthonic Deity"
            zone "Battlefield"
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
    maxInDeck 1
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
    maxInDeck 1
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
            zone "Battlefield"
        }
    }
}

card('Lernaean Hydra') {
    creature "Greek"
    flavor "A many-headed, serpent-like creature that guarded an Underworld entrance beneath Lake Lerna."
    maxInDeck 1
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
            zone "Battlefield"
        }
    }
}

card('The Underworld') {
    creature "Greek Location"
    flavor "The realm of the Dead."
    maxInDeck 1
    attack 0
    noAttack()
    health 8
    manaCost 20
    sickness 1
    // Add +2/+2 to all Chthonic Deities on the field.
    whilePresent {
        change ATTACK, HEALTH by 2 withPriority 1 onCards {
            creatureType "Greek Chthonic Deity"
            zone "Battlefield"
        }
    }
    // Add +5 attack to this card if Hades is on the field.
    /*
    ifPresent (card "HADES" zone "Battlefield") {
        change ATTACK by 5 withPriority 2 onCards { thisCard() }
    }
    */
}

card('CHARON') {
    creature "Greek Chthonic Deity"
    flavor "The Ferryman of the Underworld."
    maxInDeck 1
    attack 1
    health 4
    manaCost 10
    sickness 1
    // When this card destroys another creature card and that card 
    // goes to the graveyard, this card gains +1/+1.
    /*
    onKill {
        change ATTACK, HEALTH by 1 onCards { thisCard() }
    }
    */
}

card('EMPUSA') {
    creature "Greek Chthonic Deity"
    flavor "A monstrous underworld spirit with flaming hair, the leg of a goat and a leg of bronze."
    maxInDeck 1
    attack 1
    health 2
    manaCost 5
    sickness 1
    taunt()
}

card('MOIRAI') {
    creature "Greek Chthonic Deity"
    flavor "The white-robed incarnations of Fate."
    maxInDeck 1
    attack 1
    health 3
    manaCost 10
    sickness 1
    /*
    pickAction (
        // (3) Look at the top three cards of your deck, return them in any order.
        // (5) Look at the top three cards of your opponent’s deck, return them in any order.
        // (10) Your opponent discards the top three cards of his deck.
    )
    */
}

card('HECATE') {
    creature "Greek Chthonic Deity"
    flavor "Goddess of magic, witchcraft, the night, moon, ghosts and necromancy."
    maxInDeck 1
    attack 3
    health 2
    manaCost 10
    // phase()
    // When this creature destroys another creature and sends it to the graveyard, 
    // add a +1/+1 Lost Soul token to the field. 
    /*
    onKill {
        summon 1 of "Lost Soul" to "you" zone "Battlefield"
    }
    // When Hecate is sent to the graveyard, 
    // return random creature from the graveyard to your hand except Hecate.
    onDeath {
        
    }
    */
}

card('Judges of the Dead') {
    creature "Greek Chthonic Deity"
    flavor "Three judges sat in judgment on those who entered the Underworld."
    maxInDeck 1
    attack 3
    health 3
    manaCost 10
    sickness 1
}

card('LAMIA') {
    creature "Greek Chthonic Deity"
    flavor "A vampiric Underworld spirit."
    maxInDeck 1
    attack 1
    health 3
    manaCost 10
    sickness 1
    // Effect – When this creature deals damage to another creature, it gains +0/+1.
    /*
    afterAttacking {
        change HEALTH by 1 onCards { thisCard() }
    }
    */
}

card('MACARIA') {
    creature "Greek Chthonic Deity"
    flavor "The daughter of Hades and goddess of blessed death."
    maxInDeck 1
    attack 3
    health 2
    manaCost 10
    sickness 0 // Rush
    // When this creature destroys another creature, owner/player gains (2) health.
    /*
    onKill {
        heal 1 to "you"
    }
    */
}

card('PERSEPHONE') {
    creature "Greek Chthonic Deity"
    flavor "The queen of the underworld, wife of Hades and goddess of spring growth."
    maxInDeck 1
    attack 4
    health 3
    manaCost 15
    sickness 1
    // phase()
    // Owner gains (1) health at the end of each turn this card is on the field.
    onEndOfTurn {
        heal 1 to "you"
    }
}

card('THANATOS') {
    creature "Greek Chthonic Deity"
    flavor "Spirit of death and minister of the Underworld."
    maxInDeck 1
    attack 2
    health 2
    manaCost 5
    sickness 1
    // plague()
}

ard('TARTARUS') {
    creature "Greek Chthonic Deity"
    flavor "The primeval god of the dark, stormy pit of the Underworld, the Tartarean pit that houses the Titans."
    maxInDeck 1
    attack 3
    health 2
    manaCost 10
    sickness 1
    // When this creature comes into play, add two +2/+2 Titan Tokens to the Field.
    afterPlay {
        summon 2 of "Titan 2/2" to "you" zone "Battlefield"
    }
}

card('Mount Olympus') {
    creature "Greek Location"
    flavor "Paradise for the Gods."
    maxInDeck 1
    attack 0
    noAttack()
    health 8
    manaCost 20
    sickness 1
    // Add +2/+2 to all gods on the field.
    whilePresent {
        change ATTACK, HEALTH by 2 withPriority 1 onCards {
            creatureType "Greek God"
            zone "Battlefield"
        }
    }
    // Add +5 attack to this card if Zeus is on the field.
    /*
    ifPresent (card "ZEUS" zone "Battlefield") {
        change ATTACK by 5 withPriority 2 onCards { thisCard() }
    }
    */
}

// GODS

card('Appollo') {
    creature "Greek God"
    flavor "God of music, arts, knowledge, healing, plague, and prophecy."
    maxInDeck 1
    attack 2
    health 4
    manaCost 5
    sickness 1
    // (3) Choose one per turn: Target player or unit gains +2 health, 
    // target player or unit looses +2 health.
    /*
    pickAction ()
        { heal 2 to "you" },
        { heal 2 to cards { target() } },
        { damage 2 to "opponent" },
        { damage 2 to cards { target() } }
    )
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
