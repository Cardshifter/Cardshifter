package net.zomis.cardshifter.ecs.base;


public class CreatureTypeComponent extends Component {

	private final String creatureType;

	public CreatureTypeComponent(String creatureType) {
		this.creatureType = creatureType;
	}
	
	public String getCreatureType() {
		return creatureType;
	}

}
