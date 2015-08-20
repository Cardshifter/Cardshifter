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

	public List<String> getCreatureTypes() {
		return Collections.unmodifiableList(creatureTypes);
	}

	public boolean hasCreatureType(String creatureType) {
		return creatureTypes.stream().anyMatch(type -> creatureType.equals(type));
	}

	@Override
	public Component copy(Entity copyTo) {
		return new CreatureTypeComponent(creatureTypes);
	}

	@Override
	public String toString() {
		return "CreatureTypeComponent [creatureType=" + String.join(",", creatureTypes) + "]";
	}

}
