package com.cardshifter.modapi.base;


import java.util.*;

public class CreatureTypeComponent extends Component implements CopyableComponent {

	private final List<String> creatureTypes;

	public CreatureTypeComponent(List<String> creatureTypes) {
		this.creatureTypes = new ArrayList<>(creatureTypes);
	}

	public CreatureTypeComponent(String creatureType) {
		creatureTypes = new ArrayList<>(1);
		creatureTypes.add(creatureType);
	}

	public boolean hasCreatureType(String creatureType) {
		return creatureTypes.stream().anyMatch(type -> creatureType.equals(type));
	}

    public boolean has(String creatureType) {
        return hasCreatureType(creatureType);
    }

    @Override
	public Component copy(Entity copyTo) {
		return new CreatureTypeComponent(creatureTypes);
	}

	@Override
	public String toString() {
		return "CreatureTypeComponent [creatureType=" + String.join(",", creatureTypes) + "]";
	}

    public boolean hasAny(String... types) {
        Set<String> lookingFor = new HashSet<>(Arrays.asList(types));
        return creatureTypes.stream().anyMatch(lookingFor::contains);
    }

    public boolean noneMatch(String... types) {
        return !hasAny(types);
    }

    public String getAllTypes() {
        return String.join(" ", creatureTypes);
    }
}
