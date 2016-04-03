package com.cardshifter.api;

import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.ErrorMessage;

public abstract class ClientIO implements IdObject {

	private String name = "";
	private final ClientServerInterface server;
    private final LogInterface logger;
	private int id;
    private boolean disconnected;

	public ClientIO(ClientServerInterface server) {
		this.server = server;
		this.id = server.newClientId();
        this.logger = server.getLogger();
	}
	
	/**
	 * Send a message to this client
	 * 
	 * @param message Message to send
	 * @throws IllegalArgumentException If message is not serializable
	 */
	public final void sendToClient(Message message) {
		logger.info("Send to " + this.getName() + ": " + message);
        if (this.disconnected) {
            return;
        }
		this.onSendToClient(message);
	}
	
	protected abstract void onSendToClient(Message data);
	
	public abstract String getRemoteAddress();
	
	public String getName() {
		return name;
	}
	
	public void sentToServer(String message) {
		logger.info("Incoming message from " + this.name + ": " + message);
		server.handleMessage(this, message);
	}
	
	public void sentToServer(Message message) {
		try {
			server.performIncoming(message, this);
		}
		catch (RuntimeException ex) {
			logger.error("Error performing incoming message from " + this, ex);
			sendToClient(ErrorMessage.server(ex.toString()));
		}
	}
	
	/**
	 * Inform the server that this client has disconnected
	 */
	protected final void disconnected() {
        this.disconnected = true;
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
	
	@Override
	public String toString() {
		return getId() + ": " + getName() + " @ " + getRemoteAddress();
	}
	
}
