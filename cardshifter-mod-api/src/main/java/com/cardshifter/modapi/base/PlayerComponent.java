package com.cardshifter.modapi.base;


public class PlayerComponent extends Component {

	private final int index;
	private final String name;

	public PlayerComponent(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public int getIndex() {
		return index;
	}
	
}
