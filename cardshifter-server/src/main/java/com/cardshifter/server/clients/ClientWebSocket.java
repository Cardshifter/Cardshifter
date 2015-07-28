package com.cardshifter.server.clients;

import com.cardshifter.api.CardshifterSerializationException;
import com.cardshifter.api.serial.ByteTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

import com.cardshifter.api.ClientIO;
import com.cardshifter.api.messages.Message;
import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public class ClientWebSocket extends ClientIO {
	private static final Logger logger = LogManager.getLogger(ClientWebSocket.class);
	
	private final WebSocket conn;
    private final ByteTransformer transformer = CardshifterIO.createByteTransformer();
    private final ObjectMapper jsonMapper = CardshifterIO.mapper();

    /**
     * true if this client uses Base64
     * null if it is unknown
     * false if this client uses JSON
     *
     * This is not the best way of doing this, but it works, and right now I just want it to work.
     */
    private Boolean knownBase64client;

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
        if (!conn.isOpen()) {
            this.disconnected();
            return;
        }
		String data;
		try {
            if (knownBase64client == null) {
                logger.error("It is not yet known whether or not client is Base64 or JSON, " +
                        "ignoring sending of " + message + " to " + this);
                return;
            }

            if (knownBase64client) {
                byte[] bytes = transformer.transform(message);
                data = Base64Utils.toBase64(bytes);
                logger.info("Sending to client: " + message + " - " + Arrays.toString(bytes));
                conn.send(data);
            } else {
                try {
                    data = jsonMapper.writeValueAsString(message);
                    logger.info("Sending to client: " + message + " - " + data);
                    conn.send(data);
                } catch (JsonProcessingException e) {
                    throw new CardshifterSerializationException(e);
                }
            }
		} catch (CardshifterSerializationException e) {
            throw new RuntimeException("Error serializing message " + message + " to " + this, e);
        } catch (WebsocketNotConnectedException ex) {
            this.disconnected();
            logger.error("Websocket not connected: " + this, ex);
        }
	}

	@Override
	public String getRemoteAddress() {
		return conn.getRemoteSocketAddress().toString();
	}

    public void handleMessage(String message) throws CardshifterSerializationException {
        if (knownBase64client == null) {
            knownBase64client = Base64Utils.isBase64(message, 0, 5);
        }

        if (knownBase64client) {
            byte[] bytes = Base64Utils.fromBase64(message);
            logger.info("Connection message from: " + conn + ": " + Arrays.toString(bytes));
            this.sentToServer(transformer.readOnce(new ByteArrayInputStream(bytes)));
        } else {
            try {
                Message messageObject = jsonMapper.readValue(message, Message.class);
                logger.info("Connection message from: " + conn + ": " + messageObject);
                this.sentToServer(messageObject);
            } catch (IOException e) {
                throw new RuntimeException("Could not read JSON expected message from " + this +
                        ", message was: " + message, e);
            }
        }
    }
}
