card('Conscript') {
    creature "Bio"
    health 2
    sickness 0
    manaCost 2
    attack 2
    flavor "He just signed up last week and he is very excited to fight."
}
card('Longshot') {
    creature "Bio"
    health 1
    denyCounterAttack 1
    manaCost 3
    attack 3
    flavor "Eyes and reflexes augmented for maximum deadliness."
}
card('Bodyman') {
    creature "Bio"
    manaCost 4
    attack 2
    health 3
    flavor "Strength augmented with mechanical musculature."
}
card('Vetter') {
    creature "Bio"
    health 3
    manaCost 5
    attack 3
    flavor "A retired conscript with a desire to jack and make some quick creds."
}
card('Field Medic') {
    creature "Bio"
    health 5
    onEndOfTurn {
        heal 1 to 'you'
    }
    manaCost 5
    attack 1
    flavor "Unsung hero responsible for keeping countless troops alive."
}
card('Wastelander') {
    creature "Bio"
    health 4
    manaCost 6
    attack 4
    flavor "Spent his life learning the lessons of the wastelands."

}
card('Commander') {
    creature "Bio"
    health 3
    sickness 0
    manaCost 6
    attack 5
    flavor "A professional soldier for the government."

}
card('Cyberpimp') {
    creature "Bio"
    health 5
    manaCost 6
    attack 3
    flavor "Supersized and heavily augmented."

}
card('Cyborg') {
    creature "Bio"
    health 5
    manaCost 7
    attack 5
    flavor "He’s more machine than human now."

}
card('Web Boss') {
    creature "Bio"
    health 6
    manaCost 8
    attack 6
    flavor "Leader of a gang that primarily operates on the web."

}
card('Inside Man') {
    creature "Bio"
    health 6
    attack 2
    noAttack()
    afterPlay {
        summon 1 of 'Bodyman' to 'owner' zone 'Battlefield'
    }
    manaCost 8
    flavor "A government official with wider web control. Usually brings friends."
}
