println 'test'

def clearState = {
    assert game.getGameState() == com.cardshifter.modapi.base.ECSGameState.RUNNING
    def players = game.players
    assert currentPlayer == null
    for (def ent : players) {
        entity ent uses 'Mulligan' by ent withTargets 0 ok
    }
}

from clearState test 'enchant without scrap' using {
    def handCard = to you zone 'Hand' create 'Bionic Arms'
    def creature = to you zone 'Battlefield' create 'Cyberpimp'

    assert you.scrap == 0
    expect failure when handCard uses 'Enchant' ok
    assert you.scrap == 0
}

from clearState test 'play Inside Man' using {
    def handCard = to you zone 'Hand' create 'Inside Man'
    println 'handCard debug ' + handCard.debug()
    assert handCard.owner == you
    for (int i = 0; i < 8; i++) {
        uses 'End Turn' ok
        uses 'End Turn' ok
    }

    uses 'Play' on handCard ok
    assert you.battlefield.size() == 3

    def card = to you zone 'Battlefield' create 'Inside Man'
    assert you.battlefield.size() == 6
}

from clearState test 'fight kill and no-kill' using {
    def attacker = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 3
        health 3
    }
    def defender = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 2
        health 3
    }

    uses 'End Turn' ok
    uses 'End Turn' ok

    uses 'Attack' on attacker withTarget defender ok
    assert defender.removed
    assert !attacker.removed
    assert attacker.health == 1
}

from clearState test 'fight kill and no-kill 2/2 vs 1/1' using {
    def attacker = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 2
        health 2
    }
    def defender = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 1
        health 1
    }

    uses 'End Turn' ok
    uses 'End Turn' ok

    uses 'Attack' on attacker withTarget defender ok
    assert defender.removed
    assert !attacker.removed
    assert attacker.health == 1
}

from clearState test 'attack opponent with enchanted rush' using {
    uses 'End Turn' ok

    def enchantedCreature = to you zone 'Battlefield' create {
        creature 'Bio'
        attack 4
        health 4
    }

    def enchantment = to you zone 'Hand' create {
        println 'Sickness is ' + SICKNESS
        enchantment()
        afterPlay {
            set SICKNESS to 0 on targets
        }
    }
    expect failure when enchantedCreature uses 'Attack' ok
    uses 'Enchant' on enchantment withTarget enchantedCreature ok

    assert enchantedCreature.sickness == 0

    allowed 'Attack' on enchantedCreature ok

    int originalLife = opponent.health
    uses 'Attack' on enchantedCreature withTarget opponent ok
    assert opponent.health == originalLife - 4

}

from clearState test 'max mana' using {
    for (int i = 0; i < 30; i++) {
        uses 'End Turn' ok
    }
    assert you.mana == 10
}

from clearState test 'heal at end of turn' using {
    def healer = to you zone 'Hand' create {
        creature 'Bio'
        health 1
        afterPlay {
            damage 1 on 'you'
        }
        onEndOfTurn {
            heal 1 on 'you'
        }
    }
    def player = you
    assert player.health == 30
    uses 'Play' on healer ok
    assert player.health == 29
    uses 'End Turn' ok
    assert player.health == 30
}

from clearState test 'deny counter_attack' using {
    def creature = {
        creature 'Bio'
        attack 2
        health 4
        deny_counterattack 1
        sickness 0
    }
    def attacker = to you zone 'Battlefield' create creature
    def defender = to opponent zone 'Battlefield' create creature

    int originalLife = attacker.health
    uses 'Attack' on attacker withTarget defender ok
    assert attacker.health == originalLife
}

from clearState test 'negative mana cost' using {
    def superCard = {
        creature 'Mech'
        manaCost 1
        health 1
        whilePresent {
            change MANA_COST by -2 withPriority 1 on {
                ownedBy "you"
                zone "Hand"
            }
        }
    }
    def playCard = to you zone 'Hand' create superCard
    def handCard = to you zone 'Hand' create superCard

    assert you.mana == 1
    uses 'Play' on playCard ok
    assert you.mana == 0
    assert handCard.mana_cost == -1
    uses 'Play' on handCard ok
    assert you.mana == 0

}

from clearState test 'scrap' using {
    def scrappy = {
        creature 'Mech'
        manaCost 1
        health 1
        scrap 1
    }
    def card = to you zone 'Hand' create scrappy

    uses 'Play' on card ok
    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'Scrap' on card ok
    assert you.scrap == 1
}

from clearState test 'enchant' using {
    def card = to you zone 'Battlefield' create {
        creature 'Bio'
        attack 0
        health 1
    }
    def scrappy = to you zone 'Hand' create {
        enchantment()
        afterPlay {
            change ATTACK by 3 on targets
            change HEALTH by 2 on targets
        }
        scrapCost 1
    }

    assert you.scrap == 0
    you.scrap = 10
    assert you.scrap == 10

    def targets = uses 'Enchant' on scrappy getAvailableTargets()
    assert targets.size() == 1

    uses 'Enchant' on scrappy withTarget card ok
    assert scrappy.removed
    assert you.scrap == 9
    assert card.attack == 3
    assert card.health == 3
}

from clearState test 'heal creatures at end of turn' using {
    def attacker = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 2
        health 2
    }
    def defender = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 1
        health 4
    }

    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'Attack' on attacker withTarget defender ok
    assert attacker.health == 1
    assert defender.health == 2
    uses 'End Turn' ok
    assert attacker.health == 2
    assert defender.health == 4
}

