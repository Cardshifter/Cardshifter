package com.cardshifter.server.model;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.ServerErrorMessage;


public abstract class ClientIO implements IdObject {

	private static final Logger logger = LogManager.getLogger(ClientIO.class);
	
	private String name = "";
	private final Server server;
	private int id;
	
	public ClientIO(Server server) {
		this.server = server;
	}
	
	/**
	 * Send a message to this client
	 * 
	 * @param message Message to send
	 * @throws IllegalArgumentException If message is not serializable
	 */
	public final void sendToClient(Message message) throws IllegalArgumentException {
		logger.info("Send to " + this.getName() + ": " + message);
		this.onSendToClient(message);
	}
	
	protected abstract void onSendToClient(Message data);
	
	public String getName() {
		return name;
	}
	
	public void sentToServer(String message) {
		logger.info("Incoming message from " + this.name + ": " + message);
		server.handleMessage(this, message);
	}
	
	public void sentToServer(Message message) {
		try {
			server.getIncomingHandler().perform(message, this);
		}
		catch (RuntimeException ex) {
			logger.error("Error performing incoming message from " + this, ex);
			sendToClient(new ServerErrorMessage(ex.toString()));
		}
	}
	
	/**
	 * Inform the server that this client has disconnected
	 */
	protected void disconnected() {
		server.onDisconnected(this);
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

	@Override
	public int getId() {
		return id;
	}
	
	void setId(int id) {
		this.id = id;
	}
	
}
