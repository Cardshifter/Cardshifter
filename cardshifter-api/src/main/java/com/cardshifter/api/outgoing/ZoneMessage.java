package com.cardshifter.api.outgoing;

import java.util.Arrays;

import com.cardshifter.api.ArrayUtil;
import com.cardshifter.api.messages.Message;

/** Message containing properties of a zone. */
public class ZoneMessage extends Message {
//	SERVER: command: zone, name: 'Deck', owner: 0, id: 7, (playerIndex), size: 42, hidden: true

	private int id;
	private String name;
	private int owner;
	private int size;
	private boolean known;
	private int[] entities;

	/** Constructor. (no params) */
	public ZoneMessage() {
		this(0, "", 0, 0, false, new int[]{});
	}
	/**
	 * Constructor.
	 * @param id  The Id of this zone
	 * @param name  The name of this zone
	 * @param owner  The owner of this zone
	 * @param size  The size of this zone
	 * @param known  Whether this zone is known or hidden
	 * @param entities  Set of entities in this zone
	 */
	public ZoneMessage(int id, String name, int owner, int size, boolean known, int[] entities) {
		super("zone");
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.size = size;
		this.known = known;
		this.entities = ArrayUtil.copyOf(entities);
	}
	/** @return  The Id of this zone */
	public int getId() {
		return id;
	}
	/** @return  The name of this zone */
	public String getName() {
		return name;
	}
	/** @return  The owner of this zone */
	public int getOwner() {
		return owner;
	}
	/** @return  The size of this zone */
	public int getSize() {
		return size;
	}
	/** @return  Whether this zone is known or hidden */
	public boolean isKnown() {
		return known;
	}
	/** @return  Set of entities in this zone */
	public int[] getEntities() {
		return ArrayUtil.copyOf(entities);
	}
	/** @return  This message as converted to String  */
	@Override
	public String toString() {
		return "ZoneMessage ["
			+ "id=" + id 
			+ ", name=" + name 
			+ ", owner=" + owner 
			+ ", size=" + size 
			+ ", known=" + known 
		+ "]";
	}
	
}
