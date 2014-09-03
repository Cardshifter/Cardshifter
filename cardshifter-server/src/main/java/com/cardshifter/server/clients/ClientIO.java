package com.cardshifter.server.clients;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.messages.Message;
import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


public abstract class ClientIO {

	private static final Logger logger = LogManager.getLogger(ClientIO.class);
	
	private String name = "";
	private final Server server;
	private final ObjectWriter writer = new ObjectMapper().writer();
	
	public ClientIO(Server server) {
		this.server = server;
	}
	
	/**
	 * Send a message to this client
	 * 
	 * @param data Message to send
	 */
	public final void sendToClient(String data) {
		logger.info("Send to " + this.name + ": " + data);
		onSendToClient(data);
	}
	
	/**
	 * Send a message to this client
	 * 
	 * @param message Message to send
	 * @throws IllegalArgumentException If message is not serializable
	 */
	public final void sendToClient(Message message) throws IllegalArgumentException {
		logger.debug("Send to " + this.name + ": " + message);
		String data;
		try {
			data = writer.writeValueAsString(message);
		} catch (JsonProcessingException e) {
			String error = "Error occured when serializing message " + message;
			logger.fatal(error, e);
			throw new IllegalArgumentException(error, e);
		}
		this.sendToClient(data);
	}
	
	protected abstract void onSendToClient(String data);
	
	public String getName() {
		return name;
	}
	
	public void sentToServer(String message) {
		logger.info("Incoming message from " + this.name + ": " + message);
		server.handleMessage(this, message);
	}
	
	/**
	 * Disconnect this client
	 */
	public abstract void close();
	
	public boolean isLoggedIn() {
		return name.length() > 0;
	}

	public String getStatus() {
		return isLoggedIn() ? "online" : "offline";
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
