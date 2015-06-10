card('E.M.P.') {
	manaCost 8
	spell {
		targets 1 to 10 {
			creatureType "mech"
			zone "Battlefield"
		}
	}
	afterPlay {
		damage 2 to targets
	}
}