def clearState = {
    assert game.getGameState() == com.cardshifter.modapi.base.ECSGameState.RUNNING
    def players = game.players
    assert currentPlayer == null
    for (def ent : players) {
        entity ent uses 'Mulligan' by ent withTargets 0 ok
    }
}

from clearState test 'spellcards' using {
    def spell = to you zone 'Battlefield' create 'Random Do Nothing'
    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'End Turn' ok

}

from clearState test 'trample' using {
    def trample = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 5
        health 5
        sickness 0
        trample 1
    }
    def defender = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 4
        health 3
    }
    assert opponent.health == 30
    uses 'Attack' on trample withTarget defender ok
    assert opponent.health == 28
    assert !trample.removed
    assert defender.removed
}

from clearState test 'trample not for defender' using {
    def trampleCreature = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 5
        health 5
        sickness 0
        trample 1
    }
    def defender = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 9
        health 3
        trample 1
    }
    assert you.health == 30
    assert opponent.health == 30
    uses 'Attack' on trampleCreature withTarget defender ok
    assert you.health == 30
    assert opponent.health == 28
    assert trampleCreature.removed
    assert defender.removed
}

from clearState test 'spellcards' using {
    def spell = to you zone 'Hand' create 'Destroy Spell'
    def targetCreature = to opponent zone 'Battlefield' create {
        creature 'Mech'
        attack 1
        health 4
    }

    uses 'End Turn' ok
    uses 'End Turn' ok
    uses 'Use' on spell withTarget targetCreature ok
    assert targetCreature.removed
    assert spell.removed
}

from clearState test 'Pick One Change' using {
    def card = to you zone 'Battlefield' create 'Pick One Change'

    assert card.attack == 0
    assert card.health == 2
    uses 'End Turn' ok
    assert card.attack == 1 || card.health == 3 || card.sickness == 2

    uses 'End Turn' ok
}

from clearState test 'enchantment with afterPlay' using {
    def enchant = to you zone 'Hand' create {
        enchantment()
        afterPlay {
            change ATTACK by 1 on targets
        }
    }
    def creatur = to you zone 'Battlefield' create {
        creature 'Bio'
        attack 1
        health 1
    }

    assert creatur.attack == 1
    uses 'Enchant' on enchant withTarget creatur ok
    assert creatur.attack == 2
}

from clearState test 'enchantment non bio' using {
    def enchant = to you zone 'Hand' create {
        enchantment()
        afterPlay {
            change ATTACK by 1 on targets
        }
    }
    def creatur = to you zone 'Battlefield' create {
        creature 'Mech'
        attack 1
        health 1
    }

    def player = you
    expect failure when enchant uses 'Enchant' withTarget creatur ok
}

from clearState test 'perish' using {
    def card = to you zone 'Battlefield' create {
        creature 'Mech'
        health 1
        onEndOfTurn {
            perish()
        }
    }

    assert !card.removed
    uses 'End Turn' ok
    assert card.removed
}

from clearState test 'destroy' using {
    def card = to you zone 'Battlefield' create {
        creature 'Mech'
        health 999
    }
    def spell = to you zone 'Hand' create {
        spell {
            targets 1 cards {
                creature true
            }
            afterPlay {
                destroy targets
            }
        }
    }

    assert !card.removed
    uses 'Use' on spell withTarget card ok
    assert card.removed
}

String getDescription(entity) {
    entity.getComponent(net.zomis.cardshifter.ecs.effects.EffectComponent.class).getDescription()
}

from clearState test 'atRandom description' using {
    def card = to you zone 'Hand' create {
        afterPlay {
            pick 1 atRandom ({ perish() }, { drawCard 'all', 1 })
        }
    }

    assert getDescription(card) == 'Choose 1 at random:\n - Perish\n - All players draw 1 card'
}

from clearState test 'onStartOfTurn description' using {
    def card = to you zone 'Hand' create {
        onStartOfTurn {
            perish()
        }
    }

    assert getDescription(card) == 'At the start of your turn, perish'
}

from clearState test 'multiple effects description' using {
    def card = to you zone 'Hand' create {
        onStartOfTurn {
            perish()
            destroy 'you'
        }
    }

    assert getDescription(card) == 'At the start of your turn, perish\nAt the start of your turn, destroy you'
}

from clearState test 'multiple onDeath effects description' using {
    def card = to you zone 'Hand' create {
        onDeath {
            perish()
            destroy 'you'
        }
    }

    assert getDescription(card) == 'When this dies, perish\nWhen this dies, destroy you'
}

from clearState test 'whilePresent description' using {
    def card = to you zone 'Hand' create {
        whilePresent {
            change HEALTH by -1 withPriority 1 on {
                thisCard()
            }
        }
    }

    assert getDescription(card) == 'As long as this is on the battlefield, give this card -1 HEALTH'
}

from clearState test 'whilePresent description multiple effects' using {
    def card = to you zone 'Hand' create {
        whilePresent {
            change HEALTH by -1 withPriority 1 on {
                thisCard()
            }
            change ATTACK by +1 withPriority 1 on {
                thisCard()
            }
        }
    }

    assert getDescription(card) == 'As long as this is on the battlefield, give this card -1 HEALTH\n' +
                                   'As long as this is on the battlefield, give this card 1 ATTACK'
}

from clearState test 'description number of targets when count is 1' using {
    def card = to you zone 'Hand' create {
        spell {
            targets 1 cards {
                creatureType 'Bio'
            }
        }
        afterPlay {
            damage 1 on targets
        }
    }

    assert getDescription(card) == 'Deal 1 damage to 1 target'
}

from clearState test 'description number of targets when count is 2' using {
    def card = to you zone 'Hand' create {
        spell {
            targets 2 cards {
                creatureType 'Bio'
            }
        }
        afterPlay {
            damage 1 on targets
        }
    }

    assert getDescription(card) == 'Deal 1 damage to 2 targets'
}

from clearState test 'description includes range of number of targets' using {
    def card = to you zone 'Hand' create {
        spell {
            targets 1 to 2 cards {
                creatureType 'Bio'
            }
        }
        afterPlay {
            damage 1 on targets
        }
    }

    assert getDescription(card) == 'Deal 1 damage to 1 to 2 targets'
}

from clearState test 'negated filter' using {
    def spell = to you zone 'Hand' create {
        spell {
            targets 1 cards {
                not {
                    creatureType 'Bio'
                }
            }
        }
    }
    def bio = to you zone 'Battlefield' create {
        creature 'Bio'
        health 1
    }
    def mech = to you zone 'Battlefield' create {
        creature 'Mech'
        health 1
    }

    expect failure when spell uses 'Use' withTarget bio ok
    uses 'Use' on spell withTarget mech ok
}

from clearState test 'complex negated filter' using {
    def spell = to you zone 'Hand' create {
        spell {
            targets 1 cards {
                not {
                    creatureType "Greek"
                    ownedBy "opponent"
                }
                zone 'Battlefield'
            }
        }
    }

    def yourGreek = to you zone 'Battlefield' create {creature 'Greek'}
    def yourRoman = to you zone 'Battlefield' create {creature 'Roman'}
    def theirGreek = to opponent zone 'Battlefield' create {creature 'Greek'}
    def theirRoman = to opponent zone 'Battlefield' create {creature 'Roman'}

    def targets = uses 'Use' on spell getAvailableTargets()
    def expected = [yourGreek, yourRoman, theirRoman]
    assert targets.containsAll(expected)
    assert targets.size() == expected.size() // Shouldn't include anything else
}

from clearState test 'multiple negated filters' using {
    def spell = to you zone 'Hand' create {
        spell {
            targets 1 cards {
                not {
                    creatureType "Greek"
                }
                not {
                    ownedBy "opponent"
                }
                zone 'Battlefield'
            }
        }
    }
    def yourGreek = to you zone 'Battlefield' create {creature 'Greek'}
    def yourRoman = to you zone 'Battlefield' create {creature 'Roman'}
    def theirGreek = to opponent zone 'Battlefield' create {creature 'Greek'}
    def theirRoman = to opponent zone 'Battlefield' create {creature 'Roman'}

    def targets = uses 'Use' on spell getAvailableTargets()
    assert targets == [yourRoman]
}

from clearState test 'multiple targets: too few' using {
    def spell = to you zone 'Hand' create {
        spell {
            targets 2 to 4 cards {
                zone 'Battlefield'
            }
        }
        afterPlay {
            damage 1 on targets
        }
    }
    def cards = (0..5).collect {to you zone 'Battlefield' create {creature 'Greek'}}

    expect failure when spell uses 'Use' withTarget cards.head() ok
}

from clearState test 'multiple targets: min count' using {
    def spell = to you zone 'Hand' create {
        spell {
            targets 2 to 4 cards {
                zone 'Battlefield'
            }
        }
        afterPlay {
            damage 1 on targets
        }
    }
    def cards = (0..5).collect {to you zone 'Battlefield' create {creature 'Greek'}}

    expect ok when spell uses 'Use' withTargets cards.take(2) ok
}

from clearState test 'multiple targets: max count' using {
    def spell = to you zone 'Hand' create {
        spell {
            targets 2 to 4 cards {
                zone 'Battlefield'
            }
        }
        afterPlay {
            damage 1 on targets
        }
    }
    def cards = (0..5).collect {to you zone 'Battlefield' create {creature 'Greek'}}

    expect ok when spell uses 'Use' withTargets cards.take(4) ok
}

from clearState test 'multiple targets: too many' using {
    def spell = to you zone 'Hand' create {
        spell {
            targets 2 to 4 cards {
                zone 'Battlefield'
            }
        }
        afterPlay {
            damage 1 on targets
        }
    }
    def cards = (0..5).collect {to you zone 'Battlefield' create {creature 'Greek'}}

    expect failure when spell uses 'Use' withTargets cards.take(5) ok
}

from clearState test 'multiple targets with only one allowed' using {
    def spell = to you zone 'Hand' create {
        spell {
            targets 1 cards {
                zone 'Battlefield'
            }
        }
        afterPlay {
            damage 1 on targets
        }
    }
    def cards = (0..5).collect {to you zone 'Battlefield' create {creature 'Greek'}}

    expect failure when spell uses 'Use' withTargets cards.take(2) ok
}
