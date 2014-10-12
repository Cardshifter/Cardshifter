package com.cardshifter.modapi.base;


public class CreatureTypeComponent extends Component implements CopyableComponent {

	private final String creatureType;

	public CreatureTypeComponent(String creatureType) {
		this.creatureType = creatureType;
	}
	
	public String getCreatureType() {
		return creatureType;
	}

	@Override
	public Component copy(Entity copyTo) {
		return new CreatureTypeComponent(creatureType);
	}

	@Override
	public String toString() {
		return "CreatureTypeComponent [creatureType=" + creatureType + "]";
	}

}
