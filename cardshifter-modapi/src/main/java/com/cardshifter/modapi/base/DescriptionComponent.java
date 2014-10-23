package com.cardshifter.modapi.base;


public class DescriptionComponent extends Component {

	private final String description;

	public DescriptionComponent(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		return "Name:" + description;
	}
	
}
