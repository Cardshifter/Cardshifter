###Cardshifter modding documentation

---

#Card Library Guide

Creating and modifying the available cards is the quickest and easiest way to customize your game modification _[mod]_. This guide will explain in a detailed manner what are the different attributes, properties and effects, and how to use them effectively. 

The card library uses Plain Old JavaScript Object Notation _[POJSON]_ to store card information. This is not to be confused with the more typical JSON. JSON uses quotation marks for all field identifiers and values. POJSON only uses quotation make for String data, e.g., `"Hello, Cardshifter!"`. All identifiers, as well as numeric and boolean (`true/false`) values should be written with no quotation marks.

##General attributes

###`name`

- **Required**
- The unique identifier of a specific card. You must not have multiple cards with the same name, otherwise the game behavior could be unpredictable. 

Usage:

    {
        name: "My new card",
    },
    
###`flavor`

- Optional
- Flavor text that says something interesting about the card. 
 
Usage:

    {
        name: "My new card",
        flavor: "This is my favorite card of all. -Phrancis",
    },

##Creature attributes

Creature cards are the backbone of many Trading Card Games _[TCG]_. They are played upon the battlefield and remain until their health is depleted. The available properties are as follow.

###`creature`

- **Required** for creature type cards.
- Determines the creature type of the card. The available types are explained below. If you wish for a new creature type to be created, please [create a new issue](https://github.com/Cardshifter/Cardshifter/issues) and mark it with the "mod" label. Please be as specific as possible as to exactly how you want the new creature type to behave. 

####`creature: "Bio"`

`"Bio"` creatures are the most common. They have no special/unique property, and remain in play until their health is 0 or less. 

Usage:

    {
        name: "My new Bio",
        creature: "Bio",
        health: 1,
    },

####`creature: "Mech"`, `scrap`

`"Mech"` type creatures can be sacrified (or _scrapped_) after being played and exhausting their casting `sickness`. This destroys the creature in exchange for the determined `scrap` resource, which can be used for various things, including playing Enchantments. _(see Enchantments and `scrapCost` sections for more details)_ `"Mech"` creatures are otherwise destroyed if their health is 0 or less. 

Usage:

    {
        name: "My new Mech",
        creature: "Mech",
        scrap: 1,
        health: 1,
    },

###Common creature properties

####`health`

A creature with `n` health can take `n` damage, and it is destroyed into the discard pile if the health reaches `0` or less. Usage: `health: n`

_Note: by default setting, Cardshifter cards will gain their health back after each turn._

####`attack`

A creature with `n` attack can cause `n` damage to a target card or player. Usage: `attack: n`

####`sickness`

A creature with `n` sickness must wait `n` turns after being played before it can perform an action. A creature with `0` sickness is often referenced as having "Rush" or immediate action. Usage: `sickness: n`

####`manaCost`

A create with `n` manaCost requires spending `n` player mana points in order to play it. Usage: `manaCost: n`

####`denyCounterAttack`

A creature with `denyCounterAttack: 1` will not be subject to the counter-attack mechanic. By default, the counter-attack mechanic makes it so that if a creature attacks another creature, and the target creature has `n` attack, the attacking creature will take `n` damage as a consequence. Setting this to `1` or `true` will override this mechanic. Usage: `denyCounterAttack: 1`
