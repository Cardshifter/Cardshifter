**Cardshifter modding documentation**

---

#Cardshifter DSL Guide - Order of things

This guide will explain how the order of events (a.k.a. "things") goes on in Cardshifter. There are many events going on while a game is in play, and this is an attempt to document that.  

---

##Beginning of game events

These events happen when starting a game and prior to the first turn being initiated for either player. This is seen from one player's client side of the server, and does not aim at being authoritative or a _"true"_ representation of what happens from the server's perspective. 

Numbered events listed to the left, with accompanying code-comments to the right and comment labels for each section. 

_For simplicity, this assumes a typical game of 30-card decks, with 5-card initial draw, and mulligan enabled._

            /** OWNER ENTITIES INITIAL DRAW **/
                    /* Owner 1 (you) card draw repeated initial 5 times */
    1. zoneChange       // add to staging Hand from staging Deck
    2. card             // show card to owner 1
    3. zoneChange       
    4. card
    5. zoneChange
    6. card
    7. zoneChange
    8. card
    9. zoneChange
    10. card
                    /* Owner 2 (opponent) card draw repeated initial 5 times */
                    /* Showing cards to owner 2 is implied, but invisible to owner 1 */
    11. zoneChange      // add to staging Hand from staging Deck
    __.                 // (showing cards to owner 2 is implied, but invisible to owner 1)
    12. zoneChange
    __.
    13. zoneChange
    __.
    14. zoneChange
    __.
    15. zoneChange
    __.
    
            /** OWNER ENTITIES MULLIGAN ACTIONS **/
                /* Note these are not numbered, as the numbers vary from game actions */
                /* The messages following mulligan would be numbered accordingly */
    __. usableAction        // mulligan is usable
    __. requestTargets      // from owner, chooses to mulligan
    __. availableTargets    // to owner, possible mulligan targets (array)
    __. useAbility          // uses mulligan on selected targets

                    /* Owner 1 (you) mulligan */
    x1. zoneChange      // remove from staging Hand back into staging Deck
    x2. zoneChange      // add to staging Hand from staging Deck
    x3. card            // show card to owner
                    /* Owner 2 (opponent) mulligan */
    y1. zoneChange      // remove from staging Hand back into staging Deck
    y2. zoneChange      // add to staging Hand from staging Deck
    _3.                 // (showing cards to owner 2 is implied, but invisible to owner 1)
    
            /** PLAYER ENTITIES **/
    16. player          // create player 1 for owner 1
    17. player          // create player 2 for owner 2
    
            /** ZONE ENTITIES **/
                    /* Player 1 zone entities */
    18. zone            // create game entity's "Cards" zone: Size of all possible cards for mod, unknown to player
    19. zone            // create player 1 "Deck": Remaining 25 cards in deck, unknown to owner 1
    20. zone            // create player 1 "Hand": Chosen 5 cards after mulligan, known to owner 1
    21. zone            // create player 1 "Battlefield": Empty, known to both owners
    22. card            // show owner 1 cards from player 1 "Hand"
    23. card
    24. card
    25. card
    26. card
                    /* Player 2 zone entities */
    27. zone            // create player 2 "Deck": Remaining 25 cards in deck, unknown to owner 1
    28. zone            // create player 2 "Hand": Chosen 5 cards after mulligan, unknown to owner 1
    29. zone            // create player 2 "Battlefield": Empty, known to both owners
    
            /** INITIALIZE GAME **/
    30. resetActions    // reset player 1 actions
    31. usableAction         // set player 1 usable actions
    32. resetActions    // reset player 2 actions
    33. usableAction        // set player 2 usable actions
            /** BEGIN GAME **/

---

##Attacking cycle events

The order of events in the attacking cycle is: 

- Activate the action: `ActionPerformEvent`
- Action is Attack: `AttackEvent` | Happens here: `afterAttacking`
- Damage is dealt: `DamageEvent` _(see Issue #292)_
- `DamageEvent` happens _(see Issue #292)_
- Entity might be removed: `EntityRemoveEvent` | Happens here: `onDeath`, `onKill`
- After `AttackEvent` | Happens here: `afterAttacked`
- `ActionPerformEvent`
