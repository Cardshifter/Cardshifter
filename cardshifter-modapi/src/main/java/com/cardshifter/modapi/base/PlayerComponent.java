package com.cardshifter.modapi.base;


public class PlayerComponent extends Component {

	private final int index;
	private String name;

	public PlayerComponent(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "PlayerComponent [index=" + index + ", name=" + name + "]";
	}

}
