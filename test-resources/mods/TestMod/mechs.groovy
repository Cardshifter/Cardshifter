
card('Spareparts') {
    creature 'Mech'
    flavor 'Cobbled together from whatever was lying around at the time.'
    attack 0
    health 1
    manaCost 0
    scrap 3
    noAttack()
    sickness 0 // rush
}
card('Gyrodroid') {
    creature "Mech"
    flavor "A flying, spherical droid that shoots weak laser beams at nearby targets."
    attack 1
    health 1
    manaCost 1
    scrap 1
    denyCounterAttack 1
    // on play 0/-1 on opponent cards for this turn
    afterPlay {
        change HEALTH by -1 on {
            creature true
            ownedBy 'opponent'
            zone 'Battlefield'
        }
    }
}
card('The Chopper') {
    creature "Mech"
    flavor "Looks like a flying circular blade with a sphere in the middle."
    attack 2
    health 1
    manaCost 2
    scrap 1
    sickness 0 // rush
    // on play damage 2 to opponent or 1 to you
    afterPlay {
        pick 1 atRandom (
                { damage 2 on 'opponent' },
                { damage 1 on 'you' }
        )
    }
}
card('Shieldmech') {
    creature "Mech"
    flavor "A small, flying shield generator droid."
    attack 1
    health 3
    manaCost 3
    scrap 1
    // creatures gain 0/+1 while this creature is present after the first turn
    whilePresent {
        change MAX_HEALTH by 1 withPriority 1 on {
            creature true
            ownedBy 'you'
            zone 'Battlefield'
        }
    }
}
card('Robot Guard') {
    creature "Mech"
    flavor "Common and inexpensive robot often use for personal protection."
    attack 2
    health 2
    manaCost 2
    scrap 1
}
card('Humadroid') {
    creature "Mech"
    flavor "You might mistake it for a human, but it won’t mistake you for a Mech."
    attack 3
    health 3
    manaCost 4
    scrap 2
    // deal 2 damage to random opponent Bio when cast
    onStartOfTurn {
        change HEALTH by -2 on 1 random {
            creatureType "Bio"
            ownedBy 'opponent'
            zone "Battlefield"
        }
    }
}
card('Assassinatrix') {
    creature "Mech"
    flavor "Humanoid in form, except for two massive cannons in place of arms."
    attack 4
    health 2
    manaCost 4
    scrap 2
    denyCounterAttack 1
}
card('Fortimech') {
    creature "Mech"
    flavor "About the only place that a person is safe during a firefight is inside one of these."
    attack 2
    health 4
    manaCost 3
    scrap 2
    // Change 0/+2 on a random Bio on your Battlefield
    onStartOfTurn {
        change HEALTH by 2 on 1 random {
            creatureType 'Bio'
            ownedBy 'you'
            zone 'Battlefield'
        }
    }
}
card('Scout Mech') {
    creature "Mech"
    flavor "The fastest mech on two legs. You don’t want to see the ones with four."
    attack 5
    health 1
    manaCost 3
    scrap 2
    sickness 0 // rush
}
card('Supply Mech') {
    creature "Mech"
    flavor "Worth more than its weight in scrap, and it is pretty heavy."
    attack 0
    health 5
    noAttack()
    sickness 0 // rush
    manaCost 3
    scrap 3

}
card('F.M.U.') {
    creature "Mech"
    flavor "The Field Medical Unit is equipped with modern laser surgical tools and a variety of remedy shots."
    health 4
    attack 0
    manaCost 4
    noAttack()
    onEndOfTurn {
        heal 1 on 'you'
    }
    scrap 2
}
card('Modleg Ambusher') {
    creature "Mech"
    flavor "Uses the legs of other bots to enhance its own speed."
    attack 5
    health 3
    sickness 0 // rush
    manaCost 6
    scrap 3
}
card('Heavy Mech') {
    creature "Mech"
    flavor "The bigger they are, the harder they fall. Eventually."
    attack 3
    health 6
    manaCost 5
    scrap 3
}
card('Waste Runner') {
    creature "Mech"
    flavor "Armored and armed with superior arms."
    attack 4
    health 4
    manaCost 5
    scrap 3
}
