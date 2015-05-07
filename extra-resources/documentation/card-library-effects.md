**Cardshifter modding documentation**

---

#Card Library Guide - Effects

Creating and modifying the available cards is the quickest and easiest way to customize your game modification _[mod]_. This guide will explain how to create custom effects for cards. Please make sure you have read the `card-library-basics` guide before proceeding. 

Card effects give special behavior to a card. Various elements can be combined flexibly in order to make cards behave in very specific ways. Please read this section very carefully before attempting to make new card effects, as the syntax is very particular. Note that `// code comments` are used throughout the examples, but the comments are not needed in the actual card data. 

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
    

##Filters

- These are used to filter the effects to a particular set of targets. 
- A filter is an object containing any of those keys. `owner`, `zone`, `creature` and `creatureType`. 

---

###`owner` filters

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
                creature: "foo",        // some creature
                creatureType: "foo",    // some creature type
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

####`creature`

A specific creature card. Make sure to use the exact `name` of the target creature, otherwise it likely won't work correctly. 

####`creatureType`

A specific `creature` type, for example `"Mech"` or `"Bio"`. Affects all creature cards of that type. 

---

#HERE BE DRAGONS

####`set`

Set some amount of a specific resource to targets. This property can only be set on Enchantments.

Example usages:

#####Set `denyCounterAttack`

    {
        name: "my card",
        //some trigger: {
            set: {
                target: "owner",
                denyCounterAttack: 1, // or denyCounterAttack: true
            },
        },
    },
    
#####Set `attack: 1`

    {
        name: "my card",
        //some trigger: {
            set: {
                target: "owner",
                attack: 1,
            },
        },
    },
    
#####Set `health: 1`

    {
        name: "my card",
        //some trigger: {
            set: {
                target: "owner",
                health: 1,
            },
        },
    },
    
