**Cardshifter modding documentation**

---

#Card Library Guide - Effects

Creating and modifying the available cards is the quickest and easiest way to customize your game modification _[mod]_. This guide will explain how to create custom effects for cards. Please make sure you have read the `card-library-basics` guide before proceeding. 

Card effects give special behavior to a card. Various elements can be combined flexibly in order to make cards behave in very specific ways. Please read this section very carefully before attempting to make new card effects, as the syntax is very particular. Note that `// code comments` (and occasional `/* inline code comments */`) are used throughout the examples, to help understand the application of the syntax. 

---

##Resources

Many effects manipulate resources (or `res:`). Following is a list of the different resources. For a description of what each resource does, please see the `card-library-basics` guide.

**Important note:** The name of the resource must always be `ALL_CAPS_WITH_UNDERSCORES` as this is what the game server is expecting. 

####Basic Resources

- `ATTACK`
- `HEALTH`
- `SICKNESS`
- `MANA_COST`
- `SCRAP`
- `SCRAP_COST`

####Behaviour-specific Resources

- `ATTACK_AVAILABLE`
- `DENY_COUNTERATTACK`
- `TAUNT`

---

##`change` vs. `set`

**It is very important to define the distinction between "changing" and "setting" the resource value of a card.** 

- `change` takes account of the current value of a resource, and changes it accordingly, in a positive or negative manner.
- `change` is generally more natural to gameplay, and therefore most frequently used.
- `set` _ignores_ the current value of a resource, and sets applicable resource values to an arbitrary value.
- `set` can be used for interesting effects, but can also drastically skew the game balance, and should be used with caution.

####Examples:

Say you have the following cards as targets (according to your `filter`):

    {
        name: "card 1",
        health: 3
    },
    {
        name: "card 2",
        health: 1
    }

And you played a card with this `change` effect:

    {
        name: "change health +1",
        afterPlay: {
            res: HEALTH,
            change: 1,
            filter: { /* some applicable filter */ }
        }
    }

The resulting affected cards would be as such:

    {
        name: "card 1",
        health: 4
    },
    {
        name: "card 2",
        health: 2
    }
    
Likewise, if you played a card with this `change` effect:

    {
        name: "change health -1",
        afterPlay: {
            res: HEALTH,
            change: -1,
            filter: { /* some applicable filter */ }
        }
    }

The resulting affected cards would be as such:

    {
        name: "card 1",
        health: 2
    },
    {
        name: "card 2",
        health: 0   // destroyed
    }
    
**But**, if instead of `change`, you played a `set` card like this:

    {
        name: "change health -1",
        afterPlay: {
            res: HEALTH,
            set: 1,
            filter: { /* some applicable filter */ }
        }
    }
    
Then, the resulting cards would be like this: 

    {
        name: "card 1",
        health: 1
    },
    {
        name: "card 2",
        health: 1
    }
    
And if you you played a `set` card like this:

    {
        name: "change health -1",
        afterPlay: {
            res: HEALTH,
            set: -1,
            filter: { /* some applicable filter */ }
        }
    }
    
Then, the resulting cards would be like this: 

    {
        name: "card 1",
        health: -1  // destroyed
    },
    {
        name: "card 2",
        health: -1  // destroyed
    }
    
Therefore, be careful to use the correct keyword, `change` or `set`, according to your intentions. 

---

##Triggers

Various triggers are available for actions to be applied on. 

####`whilePresent`

- Only works on creature cards. 
- Applies the contained effects while the card is present in battle. 
- It is an array of `MODIFIER` sent to the server's resource map. 
- Note the use of square brackets `[]` is required for this to work correctly. 
- Note that an array of `whilePresent` must be used for each resource that is modified.

Usage:

    {
        name: "my card",
        whilePresent [
            // do some things
        ],
    },

Example:

    {
        name: "my card",
        creature: "Bio",
        health: 3,
        whilePresent: [
            {   // Give all your Bio creatures +2 attack
                res: ATTACK,
                priority: 1,
                change: 2,
                filter: { creatureType: "Bio", owner: "owner" }
            },
            {   // Give all your opponent's Mech creatures -3 scrap
                res: SCRAP,
                priority: 1,
                change: -3,
                filter: { creatureType: "Mech", owner: "opponent" }
            }
        ]
    }

---

####`onEndOfTurn`

- Only works on creature cards. 
- Applies the contained effects at the end of each of the owner's turns.
- It is `EFFECT` (single effect) or array of `EFFECT` (multiple effects) sent to the server's resource map. 
- Note the use of square brackets `[]` is required for this to work correctly with multiple effects.
- Note that an array of `onEndOfTurn` must be used if multiple effects apply on end of turn. It is also OK to use array syntax for a single effect. 

Usage:

    {   // single effect
        name: "my card",
        onEndOfTurn: {
            // do something
        },
    },
    {   // multiple effects
        name: "my other card",
        onEndOfTurn: [
            {
                // do some thing
            },
            {
                // do some other thing
            }
        ]
    },
    
Example:

    {   
        name: "my card",
        onEndOfTurn: { // heal self by 1
            heal: { value: 1, target: "owner" } 
        },
    },
    {
        name: "my other card",
        onEndOfTurn: [
            { // heal self by 1
                heal: { value: 1, target: "owner" } 
            },
            { // damage opponent by 1
                damage: { value: 1, target: "opponent" }
            }
        ]
    },

---

####`afterPlay`

- Works on all cards.
- Applies the contained effects after a card is played.
- It is `EFFECT` (single effect) or array of `EFFECT` (multiple effects) sent to the server's resource map. 
- Note the use of square brackets `[]` is required for this to work correctly with multiple effects.
- Note that an array of `afterPlay` must be used if multiple effects apply on end of turn. It is also OK to use array syntax for a single effect. 

Usage:

    {   // single effect
        name: "my card",
        afterPlay: {
            // do something
        },
    },
    {   // multiple effects
        name: "my other card",
        afterPlay: [
            {
                // do some thing
            },
            {
                // do some other thing
            }
        ]
    },
    
Example:

    {   
        name: "my card",
        afterPlay: { // heal self by 1
            heal: { value: 1, target: "owner" } 
        },
    },
    {
        name: "my other card",
        afterPlay: [
            {   // Give all your Bio creatures +2 attack
                res: ATTACK,
                priority: 1,
                change: 2,
                filter: { creatureType: "Bio", owner: "owner" }
            },
            {   // Give all your opponent's Mech creatures -3 scrap
                res: SCRAP,
                priority: 1,
                change: -3,
                filter: { creatureType: "Mech", owner: "opponent" }
            }
        ]
    }
    
---

##Filters

- These are used to filter the effects to a particular set of targets. 
- A filter is an object containing any of those keys. `owner`, `zone`, `creature` and `creatureType`. 
- A variety of filters are available for effects, and will be explained in detail below.

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            filter: {
                // filters here
                owner: "foo",           // some owner
                zone: "foo",            // some zone
                creatureType: "foo",    // some creature type
                creature: "foo",        // some creature
        },
    },
    
####`owner`

This is a list of possible owners with descriptions. Note that owner values are String values, and therefore need to be contained in quotation marks. 

- `"self"`: Cards that you, the player, own.
- `"opponent"`: Cards that your opponent owns. 
- `"next"`: Cards that are owned by the next player. Synonymous to `"opponent"` unless your mod supports more than 2 players. 
- `"active"`: Cards owned by the active player. Synonymous to `"self"` unless your mod supports more than 2 players. 
- `"inactive"`: Cards owned by the inactive player(s). Synonymous to `"opponent"` unless your mod supports more than 2 players. 
- `"none"`: Cards owner by no player. There are no current game mechanics that use this. 


####`zone`

This is a list of possible zones with descriptions. Note that zone values are String values, and therefore need to be contained in quotation marks. 

- `"Battlefield"`: Creature cards that are currently in active play, i.e., in battle or on the battlefield. 
- `"Hand"`: Cards in a player's hand, not played yet.
- `"Discard"`: Cards which have been discarded from battle. Sometimes also referred to as graveyard. 
- `"Exile"`: Not currently used. Cards which are exiled, which may vary depending on the mod implementation.  
- `"Cards"`: All available cards. Not currently used as it is too meta.

####`creatureType`

A specific `creature` type, for example `"Mech"` or `"Bio"`. Affects all creature cards of that type. 

####`creature`

Whether a card is a `creature`. The `creatureType` does not matter in this case.

---

##`priority`

This defines which effects get applied in what order. In many cases, setting `priority: 1` on all effects of a specific card is fine. In cases where one effect is intended to be applied logically before another is, then `priority` can be used to determine that order. `priority` is executed/applied from lower to higher number. It supports negative values as well, although we don't recommend using them, to keep intentions clear. 