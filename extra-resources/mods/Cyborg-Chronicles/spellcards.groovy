card('E.M.P.') {
	"Electromagnetic pulse that damages electronic circuitry."
	manaCost 8
	// damage 2 to up to 10 Mechs on the Battlefield
	spell {
		targets 1 to 10 {
			creatureType "Mech"
			zone "Battlefield"
		}
	}
	afterPlay {
		damage 2 on targets
	}
}
