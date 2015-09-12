**Cardshifter modding documentation**

---

#Cardshifter DSL Guide - Basics

Creating and modifying the available cards is the quickest and easiest way to customize your game modification _[mod]_. This guide will explain the basics of the different attributes, properties and effects, and how to use them effectively. 

The card library uses a Groovy Domain-Specific Language _[DSL]_ to store card information. The language is referred to as _Cardshifter DSL_. Numeric values `(1, 2, 3.14, 42, 999999999)` , as well as Boolean values `(true / false, 1 / 0)` are written without quotes. String of character values `("hello world", "http://www.cardshifter.com/", etc.)` are written either using `'single'` or `"double"` quotation marks. Single and double quotes act the same.

---

####Comments

Note that code comments are used throughout to clarify usage. Comments on a single line or at the end of a line use two forward-slashes `// this is a comment`. Comments spanning multiple lines, or interrupting a line look like this:

    /*
     * This is
     * a multi-line comment
     */
 
Or...
 
    card("My Card") /* a comment */ {
        // a line comment
    }

---

###On precise grammar...

It is important to note that the keywords and identifiers must be typed **exactly** as listed to work. Misspelled words will not work at all. Cardshifter DSL is case sensitive: `card` and `CARD` are distinct identifiers and can not be used interchangeably.

---

##General attributes

###`card("name")`

- **Required**
- Declares the unique identifier of a specific card. You must not have multiple cards with the same name. 
- The properties/attributes/effects pertaining to that card are to all be contained within curly braces `{}` after this is declared. Please use line breaks and indentation spaces to keep the properties visually grouped together.

Usage:

    card("My Card") {
        // card properties
    }
    // or also acceptable
    card("My Card") 
    {
        // card properties
    }
    
###`flavor`

- Optional
- Flavor text that says something interesting about the card. 
 
Usage:

    card("My Card") {
            flavor "This is my favorite card of all."
    }
    
---
    
###`creature`

- Any present value declares that the card is a creature. This means that the card can be played onto the battlefield, and will be placed in the discard pile when destroyed, e.g. when `health <= 0`.
- It also displays the creature type of the card.
- For reference, the most common creature types are:

#####_"Cyborg Chronicles"_ mod

- - `"Bio"`: Creature which can have enchantments played on it. See the Enchantments section below for details. 
- - `"Mech"`: Creature which can _not_ have enchantments played on it. Mechs often have the characteristic that they can be sacrificed for scrap resource. See related `scrap` section. This property requires `scrap > 0` in order to take effect.

Usage:

    card("My Card") {
        flavor "This is my favorite card of all."
        creature "Bio"
    }

#####_"Mythos"_ mod

- There are no creature-type specific effects at this time. Creature type is used for labeling and targeting.

        card("My Greek Card") {
            flavor "This is my favorite Greek card of all."
            creature "Greek"
        }

---

##Common properties

####`health`

A creature card with `n` health can take `n` damage while it is in battle. It is destroyed and placed in the discard pile if its health reaches `0` or less. A creature with `health 0` will die immediately after being played, save some effect(s) modify health. Usage: `health n`

_Note: by default setting cards will gain their health back after each turn._

####`attack`

A creature card with `n` attack can cause `n` damage  to a target card or player while it is in battle. Usage: `attack n`


####`manaCost`

A card with `n` manaCost requires spending `n` player mana points in order to play it. Usage: `manaCost n`

#####_"Mythos"_ mod only: `manaUpkeep`

In addition to mana cost when playing a card, a card also costs mana to keep on the battlefield. This is called mana upkeep. The mana upkeep cost defaults to the mana cost of the card, but can be set separately. Usage: `manaUpkeep n`

####`sickness`

A creature card with `n` sickness must wait `n` turns after being played before it can perform an action. A creature with `0` sickness is often referred to as having "Rush" or immediate action. Usage: `sickness n`


---

##Scrap

Scrap is an additional resource that can be used which behaves differently than Mana. By adding a `scrap` value to a creature card, the card becomes "scrappable", which means it can be sacrificed, or "scrapped", instead of performing an action while on the Battlefield. This eliminates the card and awards the owner player with the amount of Scrap resource specified.

For Scrap to be available, the following configuration code must be included in the mod's **Game.groovy** file:

    SCRAP = createResource("SCRAP")
    SCRAP_COST = createResource("SCRAP_COST")

Include the scrap configuration in the `include` section of **Game.groovy**:

    include 'scrap'
    
###Scrap properties

####`scrap`

A creature card with `n` scrap value can be sacrificed from battle after exhausting its casting `sickness`, in exchange for `n` scrap resource to the player. All cards with `scrap > 0` can be sacrificed. `scrap` resources can be used for various things which have a `scrapCost`, including most enchantments. See the Enchantments section for details. 

Usage: `scrap n`

####`scrapCost`

A card with `n` scrapCost requires spending `n` player scrap points in order to play it. Usage: `scrapCost n`

---

##Modifiers

####`noAttack()`

Declares that a creature cannot attack. 

####`taunt()`

A creature card with `taunt()` will require the opponent to attack that card first before other cards or the player can be attacked.

####`denyCounterAttack()`

A card with `denyCounterAttack()` will not be subject to the counter-attack mechanic. With the default counter-attack mechanic enabled, both the target and attacker involved in an attack deal damage to their opponent. Enabling the `denyCounterAttack()` modifier will override this mechanic, leaving the attacker unaffected by the targeted creature. A creature with this modifier can be referred to as a "Ranged" or "Sniper" creature. 

####`maxInDeck`

Determines an arbitrary maximum of how many of one card can be chosen into a deck. It overrides the mod's default `maxCardsPerType` property (usually 3). Usage: `maxInDeck n`

_Note: See the_ `maxCardsPerType` _property of the_ `players` _config method in the mod's_ `Game.groovy` _file to see or change the default value._

####`token()`

A card with `token()` cannot be selected from the Deck Builder. It can only be brought into play by summoning it. See `2) Card Library - Effects.md` for more details on summoning cards.

_Note: This is a shortcut to_ `maxInDeck 0` _which would produce the same effect._

---

**Note**: More advanced effects are described in `2) Card Library - Effects.md`.
