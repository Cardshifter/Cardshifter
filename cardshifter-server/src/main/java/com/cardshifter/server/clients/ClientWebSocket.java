package com.cardshifter.server.clients;

import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

import com.cardshifter.api.messages.Message;
import com.cardshifter.server.model.ClientIO;
import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ClientWebSocket extends ClientIO {
	private static final Logger logger = LogManager.getLogger(ClientWebSocket.class);
	
	private final WebSocket conn;
	private final ObjectWriter writer = CardshifterIO.mapper().writer();
	
	public ClientWebSocket(Server server, WebSocket conn) {
		super(server);
		this.conn = conn;
	}
	
	@Override
	public void close() {
		logger.info("Manual close " + this);
		conn.close();
	}

	@Override
	protected void onSendToClient(Message message) {
		String data;
		try {
			data = writer.writeValueAsString(message);
		} catch (JsonProcessingException e) {
			String error = "Error occured when serializing message " + message;
			logger.fatal(error, e);
			throw new IllegalArgumentException(error, e);
		}
		conn.send(data);
	}

	@Override
	public String getRemoteAddress() {
		return conn.getRemoteSocketAddress().toString();
	}

}
