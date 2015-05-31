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
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ECSAttributeDefault that = (ECSAttributeDefault) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
