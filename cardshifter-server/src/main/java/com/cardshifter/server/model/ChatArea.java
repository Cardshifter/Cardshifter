package com.cardshifter.server.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.clients.ClientIO;

public class ChatArea {
	private static final Logger logger = LogManager.getLogger(ChatArea.class);
	
	// By using an id here, it could be used in a database table with Hibernate
	
	private final String name;
	private final Set<ClientIO> clients;
	private final int id;
	
	public ChatArea(int id, String name) {
		this.id = id;
		this.name = name;
		this.clients = Collections.synchronizedSet(new HashSet<>());
	}
	
	public void broadcast(String message) {
		logger.info(this + " broadcast: " + message);
		String send = "CHAT " + id + " " + message;
		clients.forEach(cl -> cl.sendToClient(send));
	}
	
	public void add(ClientIO client) {
		clients.add(client);
	}
	
	public boolean remove(ClientIO client) {
		return clients.remove(client);
	}
	
	@Override
	public String toString() {
		return "ChatArea:" + id + name;
	}
	
}
