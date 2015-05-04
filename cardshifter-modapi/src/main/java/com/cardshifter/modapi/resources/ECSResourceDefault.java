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
    public ResourceRetriever retriever() {
        return this.retriever;
    }
}
