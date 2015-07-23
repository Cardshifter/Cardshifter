card('E.M.P.') {
	"Electromagnetic pulse that damages electronic circuitry."
	manaCost 8
	// damage 2 to up to 10 Mechs on the Battlefield
	spell {
		targets 1 to 10 cards {
			creatureType "Mech"
			zone "Battlefield"
		}
	}
	afterPlay {
		damage 2 on targets
	}
}

card('Test') {
    manaCost 1
    spell {
        targets 1 cards {
            creature true
            ownedBy "opponent"
            zone "Battlefield"
        }
    }
    afterPlay {
        set HEALTH to 0 on targets
    }
}