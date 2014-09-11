package com.cardshifter.server.outgoing;

import com.cardshifter.server.messages.Message;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoneMessage extends Message {
//	SERVER: command: zone, name: 'Deck', owner: 0, id: 7, (playerIndex), size: 42, hidden: true

	private final int id;
	private final String name;
	private final int owner;
	private final int size;
	private final boolean known;

	public ZoneMessage(@JsonProperty("id") int id, @JsonProperty("name") String name, 
			@JsonProperty("owner") int owner, @JsonProperty("size") int size, @JsonProperty("known") boolean known) {
		super("zone");
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.size = size;
		this.known = known;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public int getOwner() {
		return owner;
	}
	
	public int getSize() {
		return size;
	}

	public boolean isKnown() {
		return known;
	}

	@Override
	public String toString() {
		return "ZoneMessage [id=" + id + ", name=" + name + ", owner=" + owner + ", size=" + size + ", known=" + known + "]";
	}
	
}
