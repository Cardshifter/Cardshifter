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
- It is `EFFECT` (single effect) or array of `MODIFIER` (multiple effects) sent to the server's resource map. 
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
- It is `EFFECT` (single effect) or array of `MODIFIER` (multiple effects) sent to the server's resource map. 
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
    
#`HERE BE DRAGONS`

###Targets / Filters

These are used to filter the effect to a particular set of targets. 

####`"owner"`

Applies to the owner of the affected cards.

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "owner",
        },
    },
    
####`"opponent"`

Applies to the opponent of the owner of the affected cards.

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "opponent",
        },
    },
    
####`"next"`

Applies to the next player.

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "next",
        },
    },

####`"none"`

Applies to cards with no owner. 

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "none",
        },
    },
    
####`"active"`

Applies to active owner. 

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "active",
        },
    },

####`"inactive"`

Applies to inactive owner. 

Usage:

    {
        name: "my card",
        //some trigger: {
            // do something,
            target: "inactive",
        },
    },

###Actions

These use the same keywords as the Common Properties.

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
    
