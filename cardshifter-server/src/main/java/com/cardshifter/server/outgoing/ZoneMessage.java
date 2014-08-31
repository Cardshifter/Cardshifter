package com.cardshifter.server.outgoing;

import com.cardshifter.core.Player;
import com.cardshifter.core.Zone;
import com.cardshifter.server.incoming.Message;

public class ZoneMessage extends Message {
//	SERVER: command: zone, name: 'Deck', owner: 0, id: 7, (playerIndex), size: 42, hidden: true

	private final int id;
	private final String name;
	private final int owner;
	private final int size;
	private final boolean hidden;

	public ZoneMessage(Zone zone, Player player) {
		this.id = zone.getId();
		this.name = zone.getName();
		this.owner = zone.getOwner().getIndex();
		this.size = zone.size();
		this.hidden = zone.isKnownToPlayer(player);
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

	public boolean isHidden() {
		return hidden;
	}
	
}
