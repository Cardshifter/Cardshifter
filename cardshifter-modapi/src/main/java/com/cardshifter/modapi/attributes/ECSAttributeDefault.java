package com.cardshifter.modapi.attributes;

public class ECSAttributeDefault implements ECSAttribute {
	
	private final String name;

	public ECSAttributeDefault(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
