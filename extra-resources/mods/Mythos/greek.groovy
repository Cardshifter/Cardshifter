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

////// CREATURES


//// GODS

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
    pickAction ( // #270
        // (3) Deal +1 damage to target creature or player per turn.
        { damage 1 to target cost 3 mana }
        // (5) Deal +5 damage to target creature or player per turn.
        { damage 3 to target cost 3 mana }
    )
    */
}
card('URANUS') {
    creature "Greek God"
    flavor "The God of the Heavens, father of the Titans"
    maxInDeck 1
    attack 6
    health 4
    manaCost 20
    sickness 1
    // Add +3/+0 to all Titans on the field.
    whilePresent {
        change ATTACK by 3 withPriority 1 onCards {
            creatureType "Greek Titan"
            zone "Battlefield"
        }
    }
    // If this card is sent to the graveyard, bring one +4/+2 Titan token to the field.
    onDeath {
        summon 1 of "Titan 4/2" to "you" zone "Battlefield"
    }
}
card('APOLLO') {
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
    pickAction ( // #270
        { heal 2 to "you" },
        { heal 2 to cards { target() } },
        { damage 2 to "opponent" },
        { damage 2 to cards { target() } }
    )
    */
}
card('ARES') {
    creature "Greek God"
    flavor "God of war, bloodshed and violence."
    maxInDeck 1
    attack 3
    health 4
    manaCost 10
    sickness 1
    // If this card sends another card to the graveyard as a result of battle,
    //this card can attack a second time in the same turn.
    /*
    onKill { // #272
        // #274
        set hasAttacked to false onCards { thisCard() }
    }
    */
}
card('ARTEMIS') {
    creature "Greek God"
    flavor "Virgin Goddess of the hunt, wilderness and animals."
    maxInDeck 1
    attack 2
    health 3
    manaCost 5
    sickness 1
    // When this card comes into play, add one +2/+2 Bear token onto the field.
    afterPlay {
        summon 1 of "Bear" to "you" zone "Battlefield"
    }
}
card('ATHENA') {
    creature "Greek God"
    flavor "Goddess of intelligence, skill, peace, warfare, battle strategy, and wisdom."
    maxInDeck 1
    attack 3
    health 2
    manaCost 5
    sickness 1
    // Give +1/+1 to all God cards on the field.
    whilePresent {
        change ATTACK, HEALTH by 1 withPriority 1 onCards {
            creatureType "Greek God"
            zone "Battlefield"
        }
    }
    // (3) If a player targets this card for an attack,
    // redirect that damage to another target unit or player.
    /*
    pickAction ( // #270
        {
            redirectAtRandom() // #277
        }
    )
    */
}
card('HERMES') {
    creature "Greek God"
    flavor "God of boundaries, travel, communication, trade, language, and writing."
    maxInDeck 1
    attack 2
    health 2
    manaCost 5
    sickness 1
    denyCounterAttack() // ranged
}
card('HERA') {
    creature "Greek God"
    flavor "Queen of the Gods and the wife of Zeus."
    maxInDeck 1
    attack 3
    health 2
    manaCost 5
    sickness 1
    // phase() // #262
}
card('POSEIDON') {
    creature "Greek God"
    flavor "God of the sea, rivers, floods, droughts, and earthquakes."
    maxInDeck 1
    attack 4
    health 4
    manaCost 15
    sickness 0 // Rush
    // Once per turn: Freeze (random) enemy unit for 2 turns.
    onStartOfTurn {
        set SICKNESS to 3 onCards 1 random {
            creature true
            ownedBy "opponent"
            zone "Battlefield"
        }
    }
}
card('EROS') {
    creature "Greek God"
    flavor "The God of love and attraction."
    maxInDeck 1
    attack 3
    health 2
    manaCost 10
    sickness 1
    // When Eros enters the field, take control of target creature for one turn.
    /*
    afterPlay {
        // #267
        charm 1 target for 1 turn
    }
    */
}
card('GAIA') {
    creature "Greek God"
    flavor "Personification of the Earth, mother of the Titans."
    maxInDeck 1
    attack 4
    health 4
    manaCost 15
    sickness 1
    // Add +0/+3 to all Titans on the field.
    whilePresent {
        change HEALTH by 1 withPriority 1 onCards {
            creatureType "Greek Titan"
            zone "Battlefield"
        }
    }
    // If this card is sent to the graveyard, bring two +2/+2 Titan tokens to the field.
    onDeath {
        summon 2 of "Titan 2/2" to "you" zone "Battlefield"
    }
}

//// CHTHONIC DEITIES

card('HADES') {
    creature "Greek Chthonic Deity"
    flavor "God of the Underworld."
    maxInDeck 1
    attack 8
    health 8
    manaCost 30
    sickness 1
    // plague() // #264
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
    // phase() // #262
    // When this creature is put into play, add one +2/+2 Titan Token to the field.
    afterPlay {
        summon 1 of "Titan 2/2" to "you" zone "Battlefield"
    }
    // When this creature is sent to the graveyard, add one +4/+2 Titan Token to the Field.
    onDeath {
        summon 1 of "Titan 4/2" to "you" zone "Battlefield"
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
    afterAttacked { // #266
        change ATTACK by 1 onCards { thisCard() }
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
    ifPresent (cardName "HADES" zone "Battlefield") { // #261
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
    onKill { // #272
        change ATTACK, HEALTH by 1 onCards { thisCard() }
    }
    */
    /*
    // When this card is sent to the graveyard, destroy target creature.
    onDeath {
        // #282
        set HEALTH to 0 onCards { attacker() }
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
    pickAction ( // #270
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
    // phase() // #262
    // When this creature destroys another creature and sends it to the graveyard,
    // add a +1/+1 Lost Soul token to the field.
    /*
    onKill { // #272
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
    // When this creature deals damage to another creature, it gains +0/+1.
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
    onKill { // #272
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
    // phase() // #262
    // Owner gains (1) health at the end of each turn this card is on the field.
    onEndOfTurn {
        heal 1 on "you"
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
    // plague() // #264
}
card('TARTARUS') {
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

//// LOCATIONS

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
    ifPresent (cardName "ZEUS" zone "Battlefield") { // #261
        change ATTACK by 5 withPriority 2 onCards { thisCard() }
    }
    */
}

card('Tartarean Pit') {
    creature "Greek Location"
    flavor "Prison of the Titans."
    maxInDeck 1
    attack 0
    noAttack()
    health 8
    manaCost 20
    sickness 1
    // Attack and health are equal to the number of Titans cards on the field.
    /*
    // #240
    whilePresent {
        set ATTACK, HEALTH to count({creatureType "Greek Titan"; zone "Battlefield"}) withPriority 1 onCards { thisCard() }
    }
    */
    // At the end of your turn, if a unit was sent to the graveyard
    // add two +2/+2 Titan token to the field.
    /*
    onEndOfTurn {
        ifAnyCardDied { // #280
            summon 2 of "Titan 2/2" to "you" zone "Battlefield"
        }
    }
    */
}

/// TITANS

card('Hyperion') {
    creature "Greek Titan"
    flavor "Titan of Light."
    maxInDeck 1
    attack 4
    health 2
    manaCost 15
    sickness 0 // Rush
}
 card('Mnemosyne') {
    creature "Greek Titan"
    flavor "Titaness of memory and remembrance, and mother of the Nine Muses."
    maxInDeck 1
    attack 2
    health 3
    manaCost 5
    sickness 1
    // phase() // #262
}
card('Styx') {
    creature "Greek Titan"
    flavor "Titaness of the Underworld river Styx and personification of hatred."
    maxInDeck 1
    attack 3
    health 3
    manaCost 5
    sickness 1
    // Effect – Gives +1/+0 to all Titans on the field.
    whilePresent {
        change ATTACK by 1 withPriority 1 onCards {
            creatureType "Greek Titan"
            zone "Battlefield"
        }
    }
}
card('Rhea') {
    creature "Greek Titan"
    flavor "Titaness of fertility, motherhood and the mountain wilds."
    maxInDeck 1
    attack 2
    health 1
    manaCost 5
    sickness 1
    // When this card is sent to the graveyard, bring a +2/+2 Bear token onto the field.
    onDeath {
        summon 1 of "Bear" to "you" zone "Battlefield"
    }
}
card('Menoetius') {
    creature "Greek Titan"
    flavor "Titan of violent anger, rash action, and human mortality."
    maxInDeck 1
    attack 4
    health 4
    manaCost 10
    sickness 0 // Rush
}

//// HEROES

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
            { damage 3 to 1 random { creature true; ownedBy "opponent" } }
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
card('Achilles') {
    creature "Greek Hero"
    flavor "Hero of the Trojan War."
    maxInDeck 1
    attack 3
    health 3
    manaCost 10
    sickness 1
    // dodge() // #263
}
card('Perseus') {
    creature "Greek Hero"
    flavor "Son of Zeus and the slayer of the Gorgon Medusa."
    maxInDeck 1
    attack 5
    health 4
    manaCost 15
    sickness 1
    taunt()
    // Freeze unit for 1 turn who was dealt damage from
    // or who dealt damage to this unit.
    /*
    afterAttacked { // #266
        // #281 & #282
        counterAttack set SICKNESS to 2 onCards { attacker() }
    }
    afterAttacking { // #275
        set SICKNESS to 2 onCards { target() }
    }
    */
}
card('Iolaus') {
    creature "Greek Hero"
    flavor "Nephew of Heracles who aided his uncle in one of his Labors."
    maxInDeck 1
    attack 2
    health 2
    manaCost 5
    sickness 1
    // Gains +1/+1 if Heracles is on the field.
    /*
    ifPresent (cardName "HERACLES" zone "Battlefield") { // #261
        change ATTACK, HEALTH by 1 withPriority 1 onCards { thisCard() }
    }
    */
}
card('Theseus') {
    creature "Greek Hero"
    flavor "Son of Poseidon, King of Athens and slayer of the Minotaur."
    maxInDeck 1
    attack 3
    health 2
    manaCost 5
    sickness 1
    ranged()
}
card('Odysseus') {
    creature "Greek Hero"
    flavor "King of Ithaca whose adventures are the subject of Homer’s Odyssey."
    maxInDeck 1
    attack 2
    health 1
    manaCost 5
    sickness 1
}
card('Daedalus') {
    creature "Greek Hero"
    flavor "Creator of the Labyrinth and a great inventor, until King Minos trapped him in his own creation."
    maxInDeck 1
    attack 3
    health 4
    manaCost 10
    sickness 1
    // When Daedalus is sent to the graveyard, summon a +4/+4 Minotaur token to the field.
    onDeath {
        summon 1 of "Minotaur" to "you" zone "Battlefield"
    }
}
card('Hector') {
    creature "Greek Hero"
    flavor "Hero of the Trojan War and champion of the Trojan people."
    maxInDeck 1
    attack 4
    health 4
    manaCost 15
    sickness 0 // Rush
    // Give +1/+1 to all other Hero cards on the field.
    whilePresent {
        change ATTACK, HEALTH by 1 withPriority 1 onCards {
            creatureType "Greek Hero"
            zone "Battlefield"
        }
    }
}
card('Ajax The Great') {
    creature "Greek Hero"
    flavor "Hero of the Trojan War and king of Salamis."
    maxInDeck 1
    attack 2
    health 3
    manaCost 5
    sickness 1
}

//// CREATURES

card('Medusa') {
    creature "Greek"
    flavor "A mortal woman transformed into a hideous Gorgon by Athena."
    maxInDeck 1
    attack 4
    health 3
    manaCost 15
    sickness 1
    // Freeze unit for 3 turns that was dealt or dealt damage to this unit.
    /*
    afterAttacked { // #266
        // #281 & #282
        counterAttack set SICKNESS to 4 onCards { attacker() }
    }
    afterAttacking { // #275
        set SICKNESS to 4 onCards { target() }
    }
    */
    // When Medusa is sent to the graveyard, summon three +1/+1 snake tokens to the field.
    onDeath {
        summon 3 of "Medusa Snake" to "you" zone "Battlefield"
    }
}
card('Nemean Lion') {
    creature "Greek"
    flavor "A gigantic lion whose skin was impervious to weapons; Heracles strangled it."
    maxInDeck 1
    attack 3
    health 6
    manaCost 15
    sickness 1
    taunt()
}
card('Erymanthian Boar') {
    creature "Greek"
    flavor "A gigantic boar, which Heracles was sent to retrieve as one of his labors."
    maxInDeck 1
    attack 5
    health 4
    manaCost 10
    sickness 0 // Rush
}
card('Pegasus') {
    creature "Greek"
    flavor "A divine winged stallion that is pure white, son of Medusa and Poseidon, and father of winged horses."
    maxInDeck 1
    attack 2
    health 2
    manaCost 5
    sickness 1
    // dodge() // #263
    /*
    ifPresent (creatureType "Greek Hero"; zone "Battlefield") {
        change ATTACK, HEALTH by 2 withPriority 1 onCards { thisCard() }
    }
    */
}
card('Ararchne') {
    creature "Greek"
    flavor "Half-spider half-female, she is the mother of all spiders."
    maxInDeck 1
    attack 3
    health 3
    manaCost 5
    sickness 1
    // plague() // #264
    // When Ararchne is sent to the graveyard,
    // summon two +2/+2 Spider tokens to the field with Plague.
    onDeath {
        summon 2 of "Ararchne Spider" to "you" zone "Battlefield"
    }
}
card('Griffin') {
    creature "Greek"
    flavor "A creature that combines the body of a lion and the head and wings of an eagle."
    maxInDeck 1
    attack 3
    health 2
    manaCost 5
    sickness 1
    // dodge() // #263
}
card('Caucasian Eagle') {
    creature "Greek"
    flavor "A giant eagle set by Zeus to feed on the ever-regenerating liver of Prometheus."
    maxInDeck 1
    attack 2
    health 3
    manaCost 5
    sickness 1
    // dodge() // #263
}
card('Chiron') {
    creature "Greek"
    flavor "The eldest and wisest of the centaurs, the ancient trainer of heroes."
    maxInDeck 1
    attack 4
    health 3
    manaCost 10
    sickness 0 // Rush
    // When Chiron comes into play add +1/+0 to all Creatures on the field
    afterPlay {
        change ATTACK by 1 onCards {
            creature true
        }
    }
}
card('Cerberus') {
    creature "Greek"
    flavor "The Three-headed hound that guards the gates of the Underworld."
    maxInDeck 1
    attack 3
    health 4
    manaCost 15
    sickness 1
    taunt()
    // Cerberus gains +2/+2 if Hades is on the field.
    /*
    ifPresent (cardName "HADES"; zone "Battlefield") {
        change ATTACK, HEALTH by 1 withPriority 1 onCards { thisCard() }

    }
    */
}

//// TOKENS

card('Bear') {
    creature "Greek"
    flavor ""
    attack 2
    health 2
    manaCost 0
    sickness 1
    token()
}

card('Lost Soul') {
    creature "Greek"
    flavor ""
    attack 1
    health 1
    manaCost 0
    sickness 1
    token()
}
card("Titan 2/2") {
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
card('Minotaur') {
    creature "Greek"
    flavor ""
    attack 4
    health 4
    manaCost 0
    sickness 1
    token()
}
card('Medusa Snake') {
    creature "Greek"
    flavor ""
    maxInDeck 1
    attack 1
    health 1
    manaCost 0
    sickness 1
    token()
}
card('Ararchne Spider') {
    creature "Greek"
    flavor ""
    maxInDeck 1
    attack 2
    health 2
    manaCost 0
    sickness 1
    token()
    // plague() // #264
}


////// NOT CREATURES


//// SPELLS

/* DOES NOTHING RIGHT NOW
card('Apollo’s Bow') {
    flavor ""
    maxInDeck 1
    manaCost 5
    spell {
        // Choose one effect:
        // Target player or unit gains +3 health.
        // Target player or unit looses +3 health.
        //afterPlay {
            //pickAction ( // #270
            //    { heal 3 to "you" },
            //    { heal 3 to cards { target() } }, // #282
            //    { damage 3 to "opponent" },
            //    { damage 3 to cards { target() } } // #282
            //)
        //}
    }
}
*/
/* DOES NOTHING RIGHT NOW
card('Tale of the Three Brothers') {
    flavor ""
    maxInDeck 1
    manaCost 20
    spell {
        // If you possess Hades, Poseidon, and Zeus on the field or in the graveyard
        // when this card is activated, choose any number of units your opponent controls,
        // but no more than 3, and destroy them.
        afterplay {
            // #284
                ifPresent (cardName "HADES", "POSEIDON", "ZEUS"; zone "Battlefield") {
                    destroyUpTo 3 cards { ownedBy "opponent"; zone "Battlefield" }
                }
        }
    }
}
*/
/* DOES NOTHING RIGHT NOW
card('The Wrath of Zeus') {
    flavor "hello"
    maxInDeck 1
    manaCost 15
    spell {
        // Deal +5 damage to target player or unit.
    //    // #284
    //    afterPlay {
    //        damage n to { target() }
    //    }
    }
}
*/
/* DOES NOTHING RIGHT NOW
card('The Might of Hercules') {
    flavor ""
    maxInDeck 1
    manaCost 10
    spell {
        afterPlay {
            // Give target unit +3/+0 until the end of turn.
            change ATTACK by 3 untilEndOfTurn onCards { target() } // #282 & #285
        }
        // If that target unit is a Hero give it Range as well.
        ifTarget (creatureType: "Greek Hero") { // #286
            set ranged() toCards { target() }
        }
    }
}
*/
card('Golden Fleece') {
    flavor ""
    maxInDeck 1
    manaCost 5
    enchantment()
    // Equipped unit gains +0/+3.
    addHealth 3
}
card('Achilles’ Armor') {
    flavor ""
    maxInDeck 1
    manaCost 10
    enchantment()
    addAttack 1
    addHealth 2
    // If that unit is a Hero it gains Taunt.
    /*
    spell {
        ifTarget (creatureType: "Greek Hero") { // #286
            set taunt() toCards { target() }
        }
    }
    */
}
card('Hades’ Bident') {
    flavor ""
    maxInDeck 1
    manaCost 10
    enchantment()
    addAttack 3
    // If that unit is a Chthonic Deity it gains Plague.
    /*
    spell {
        ifTarget (creatureType: "Greek Chthonic Deity") { // #286
            set taunt() toCards { target() }
        }
    }
    */
}
card('Helm of Darkness') {
    flavor ""
    maxInDeck 1
    manaCost 5
    enchantment()
    // Target unit gains +1/+1 and Phase.
    addAttack 1
    addHealth 1
    /*
    spell {
            set phase() toCards { target() }
    }
    */
}
card('Poseidon’s Trident') {
    flavor ""
    maxInDeck 1
    manaCost 10
    enchantment()
    addAttack 2
    addHealth 2
    // If that unit is a God it gains Dodge.
    /*
    spell {
        ifTarget (creatureType: "Greek God") { // #286
            set dodge() toCards { target() }
        }
    }
    */
}
card('Cronus’ Scythe') {
    flavor ""
    maxInDeck 1
    manaCost 10
    enchantment()
    addAttack 2
    addHealth 1
    // If that unit is a Titan it gains Taunt.
    /*
    spell {
        ifTarget (creatureType: "Greek Titan") { // #286
            set taunt() toCards { target() }
        }
    }
    */
}
