package com.cardshifter.server.clients;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

import com.cardshifter.server.model.ClientIO;
import com.cardshifter.server.model.Server;

public class ClientWebSocket extends ClientIO {
	private static final Logger logger = LogManager.getLogger(ClientWebSocket.class);
	
	private final WebSocket conn;
	
	public ClientWebSocket(Server server, WebSocket conn) {
		super(server);
		this.conn = conn;
	}
	
	@Override
	public void onSendToClient(String message) {
		logger.info("Send to " + conn + ": " + message);
		conn.send(message);
	}

	@Override
	public void close() {
		logger.info("Manual close " + this);
		conn.close();
	}

}
