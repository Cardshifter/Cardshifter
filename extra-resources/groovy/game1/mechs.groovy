// Cyborg-Chronicles Cards

card('Spareparts') {
    creature 'Mech'
    health 1
    attack 0
    noAttack()
    sickness 0
    scrap 0
    manaCost 0
    flavor 'Cobbled together from whatever was lying around at the time.'
}
card('Gyrodroid') {
    creature "Mech"
    health 1
    denyCounterAttack 1
    attack 1
    manaCost 1
    scrap 1
    flavor "A flying, spherical droid that shoots weak laser beams at nearby targets."
}
card('The Chopper') {
    creature "Mech"
    health 1
    sickness 0
    manaCost 2
    attack 2
    scrap 1
    flavor "Looks like a flying circular blade with a sphere in the middle."
}
card('Shieldmech') {
    creature "Mech"
    health 3
    manaCost 2
    attack 1
    scrap 1
    flavor "A small, flying shield generator droid."
}
card('Robot Guard') {
    creature "Mech"
    health 2
    manaCost 2
    attack 2
    scrap 1
    flavor "Common and inexpensive robot often use for personal protection."
}
card('Humadroid') {
    creature "Mech"
    health 3
    manaCost 3
    attack 3
    scrap 2
    flavor "You might mistake it for a human, but it won’t mistake you for a mech."

}
card('Assassinatrix') {
    creature "Mech"
    health 1
    denyCounterAttack 1
    manaCost 3
    attack 4
    scrap 2
    flavor "Humanoid in form, except for two massive cannons in place of arms."
}
card('Fortimech') {
    creature "Mech"
    health 4
    manaCost 3
    attack 2
    scrap 2
    flavor "About the only place that a person is safe during a firefight is inside one of these."
}
card('Scout Mech') {
    creature "Mech"
    health 1
    sickness 0
    manaCost 3
    attack 5
    scrap 2
    flavor "The fastest mech on two legs. You don’t want to see the ones with four."
}
card('Supply Mech') {
    creature "Mech"
    health 5
    attack 0
    noAttack true
    sickness 0
    manaCost 3
    scrap 3
    flavor "Worth more than its weight in scrap, and it is pretty heavy."

}
card('F.M.U.') {
    creature "Mech"
    health 4
    attack 0
    noAttack true
    onEndOfTurn {
        heal {
            value 1
            target "owner"
        }
    }
    manaCost 4
    scrap 2
    flavor "The Field Medical Unit is equipped with modern laser surgical tools and a variety of remedy shots."

}
card('Modleg Ambusher') {
    creature "Mech"
    health 3
    sickness 0
    manaCost 6
    attack 5
    scrap 3
    flavor "Uses the legs of other bots to enhance its own speed."
}
card('Heavy Mech') {
    creature "Mech"
    health 6
    manaCost 5
    attack 3
    scrap 3
    flavor "The bigger they are, the harder they fall. Eventually."

}
card('Waste Runner') {
    creature "Mech"
    manaCost 5
    attack 4
    health 4
    scrap 3
    flavor "Armored and armed with superior arms."

}
