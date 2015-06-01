**Cardshifter modding documentation**

---

#Card Library Guide - Spells

This guide will explain how to create custom spell cards. We created an easy-to-use, flexible system that allows for creative spells to be applied to your mod. 

Spells are cards that are played once and do not persist after playing; in other words, they are discarded from the game after they are played. 

---

###On precise grammar...

It is important to note that the keywords and identifiers must be typed **exactly** as listed to be trustworthy of working. Misspelled words will not work at all. Capilatization must also be respected to ensure functionality.

---

##Syntax

A spell card, in general, uses this type of syntax:

    card('name') {
        // declare the card is a spell
        spell {
            // declares the number of targets
            targets n {
                [filters]
            }
        }
        // declare a trigger, normally afterPlay
        afterPlay {
            [actions/effects]
        }
    }
    
The specifics of each section of the syntax will be explained in more detail.

---

##`spell` declaration

This declares that a card is a spell, which means it will not persist in play after it is played/cast, but rather moved to the graveyard. 

---

##`targets`

A spell can have either of 1, multiple, or zero targets. Depending on what targets are allowed determines how the card will be played. 

###Defined number of targets

A spell with a defined number of targets will let you choose `n` targets according to its filters.

Syntax:

    card('name') {
        spell {
            targets n {
                [filters]
            }
        }
        afterPlay {
            [actions/effects] to targets
        }
    }
    
Example: 

    // deal 1 damage to a target
    card('Fireball') {
        spell {
            targets 1 {
                creature true
                ownedBy 'opponent'
                zone 'Battlefield'
            }
        }
        afterPlay {
            damage 1 to targets
        }
    }
    
###Variable number of targets

This lets a player choose a number of targets with a minimum `m` and a maximum `n`, according to its filters, and apply the spell to the chosen number.

Syntax:

    card('name') {
        spell {
            targets m to n {
                [filters]
            }
        }
        afterPlay {
            [actions/effects] to targets
        }
    }

Examples:

    card('Healing rain') {
        spell {
            targets 2 to 4 cards {
                creature true
                ownedBy 'you'
                zone 'Battlefield'
            }
            afterPlay {
                heal 1 to targets
            }
        }
    }
    
###No target

This is used for spells which do not target a specific card, and are more "general" in nature.

Syntax:

    card('name') {
        spell()
        afterPlay {
            [actions/effects]
        }
    }

Examples:

    card('Acid rain') {
        spell()
        afterPlay {
            damage 1 to {
                zone 'Battlefield'
            }
        }
    }
    
---

##Spell effects

The effects declared after the spell targets are according to the `2) Card Library - Effects` documentation. 