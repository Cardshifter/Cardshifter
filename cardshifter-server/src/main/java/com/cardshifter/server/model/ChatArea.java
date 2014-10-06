package com.cardshifter.server.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.outgoing.ServerErrorMessage;

public class ChatArea implements IdObject {
	private static final Logger logger = LogManager.getLogger(ChatArea.class);
	
	// By using an id here, it could be used in a database table with Hibernate. It also allows multiple chats per user
	
	private final String name;
	private final Set<ClientIO> clients;
	private final int id;
	
	public ChatArea(int id, String name) {
		this.id = id;
		this.name = name;
		this.clients = Collections.synchronizedSet(new HashSet<>());
	}
	
	public void broadcast(ChatMessage message) {
		logger.info(this + " broadcast: " + message);
		clients.forEach(cl -> cl.sendToClient(message));
	}
	
	public void add(ClientIO client) {
		clients.add(client);
		broadcast(new ChatMessage(id, "Server Chat " + id, client.getName() + " has joined the chat"));
	}
	
	public boolean remove(ClientIO client) {
		return clients.remove(client);
	}
	
	@Override
	public String toString() {
		return "ChatArea:" + id + name;
	}

	public void incomingMessage(ChatMessage message, ClientIO client) {
		if (!clients.contains(client)) {
			client.sendToClient(new ServerErrorMessage("You are not inside chat " + id));
		}
		else {
			this.broadcast(new ChatMessage(id, client.getName(), message.getMessage()));
		}
	}

	@Override
	public int getId() {
		return id;
	}

	public Set<ClientIO> getUsers() {
		return Collections.unmodifiableSet(this.clients);
	}
	
}
