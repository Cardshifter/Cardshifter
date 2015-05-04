package com.cardshifter.modapi.resources;

public class ECSResourceDefault implements ECSResource {
	
	private final String name;
    private final ResourceRetriever retriever;

    public ECSResourceDefault(String name) {
		this.name = name;
        this.retriever = ResourceRetriever.forResource(this);
	}
	
	@Override
	public String toString() {
		return name;
	}

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ECSResourceDefault)) {
            return false;
        }

        ECSResource that = (ECSResource) o;

        if (!name.equals(that.toString())) {
            return false;
        }

        return true;
    }

    @Override
    public ResourceRetriever retriever() {
        return this.retriever;
    }
}
