package com.cardshifter.server.clients;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.model.Command;
import com.cardshifter.server.model.Server;


public abstract class ClientIO {

	private static final Logger logger = LogManager.getLogger(ClientIO.class);
	
	private String name = "";
	private final Server server;
	
	public ClientIO(Server server) {
		this.server = server;
	}
	
	/**
	 * Send a message to this client
	 * 
	 * @param data Message to send
	 */
	public final void sendToClient(String data) {
		logger.debug("Send to " + this.name + ": " + data);
		onSendToClient(data);
	}
	
	protected abstract void onSendToClient(String data);
	
	public String getName() {
		return name;
	}
	
	public void sentToServer(String message) {
		logger.debug("Incoming message from " + this.name + ": " + message);
		server.handleMessage(this, message);
	}
	
	/**
	 * Disconnect this client
	 */
	public abstract void close();
	
	@Deprecated
	public Command parseMessage(String input) {
		return new Command(this, input);
	}

	public boolean isLoggedIn() {
		return name.length() > 0;
	}

	public String getStatus() {
		return isLoggedIn() ? "online" : "offline";
	}
	
}
