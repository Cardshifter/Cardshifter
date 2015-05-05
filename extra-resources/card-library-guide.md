###Cardshifter modding documentation

---

#Card Library Guide

Creating and modifying the available cards is the quickest and easiest way to customize your game modification _[mod]_. This guide will explain in a detailed manner what are the different attributes, properties and effects, and how to use them effectively. 

The card library uses JavaScript Objects to store card information. This is not to be confused with typical JSON, which uses quotation marks for all field identifiers and values. POJSON only uses quotation make for String data, e.g., `"Hello, Cardshifter!"`. All identifiers, as well as numeric and boolean (`true/false`) values should be written with no quotation marks.

##General attributes

####`name`

- **Required**
- The unique identifier of a specific card. You must not have multiple cards with the same name, otherwise the game behavior could be unpredictable. 

Usage:

    {
        name: "My new card",
    },
    
####`flavor`

- Optional
- Flavor text that says something interesting about the card. 
 
Usage:

    {
        name: "My new card",
        flavor: "This is my favorite card of all. -Phrancis",
    },
    
####`creature`

- Displays the creature type of the card. This does not alter card behavior. 

##Creature attributes

Creature cards are the backbone of many Trading Card Games _[TCG]_. They are played upon the battlefield and remain until their health is depleted. The available properties are as follow.

###Common properties

####`health`

A creature with `n` health can take `n` damage, and it is destroyed into the discard pile if the health reaches `0` or less. Usage: `health: n`

_Note: by default setting, Cardshifter cards will gain their health back after each turn._

####`attack`

A creature with `n` attack can cause `n` damage to a target card or player. Usage: `attack: n`

####`scrap`

A creature with `n` scrap value can be sacrificed after exhausting its casting `sickness`, in exchange for `n` scrap resource to the player. All cards with `scrap > 0` can be sacrificed. `scrap` resource can be used for various things which have a `scrapCost`, including most Enchantments. See the Enchantments section for details. Usage: `scrap: n`

####`sickness`

A creature with `n` sickness must wait `n` turns after being played before it can perform an action. A creature with `0` sickness is often referenced as having "Rush" or immediate action. Usage: `sickness: n`

####`manaCost`

A card with `n` manaCost requires spending `n` player mana points in order to play it. Usage: `manaCost: n`

####`scrapCost`

A card with `n` scrapCost requires spending `n` player scrap points in order to play it. Usage: `scrapCost: n`

####`denyCounterAttack`

A creature with `denyCounterAttack: 1` will not be subject to the counter-attack mechanic. By default, the counter-attack mechanic makes it so that if a creature attacks another creature, and the target creature has `n` attack, the attacking creature will take `n` damage as a consequence. Setting this to `1` or `true` will override this mechanic. Usage: `denyCounterAttack: 1`
