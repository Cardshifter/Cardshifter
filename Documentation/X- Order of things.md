**Cardshifter modding documentation**

---

#Card Library Guide - Order of things

This guide will explain how the order of events (a.k.a. "things") goes on in Cardshifter. There are many events going on while a game is in play, and this is an attempt to document that.  

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