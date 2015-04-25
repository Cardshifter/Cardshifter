package com.cardshifter.server.clients;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedAction;

import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.ClientIO;
import com.cardshifter.api.incoming.TransformerMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.ServerErrorMessage;
import com.cardshifter.api.serial.CommunicationTransformer;
import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientSocketHandler extends ClientIO implements Runnable {
	private static final Logger logger = LogManager.getLogger(ClientSocketHandler.class);
	
	private Socket	socket;
	private final InputStream in;
	private final OutputStream out;
	
	private final ObjectMapper mapper = CardshifterIO.mapper();
	private CommunicationTransformer transformer;

	public ClientSocketHandler(Server server, Socket socket) throws IOException {
		super(server);
		this.socket = socket;
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		transformer = new JsonSerialization(mapper);
		in = socket.getInputStream();
		out = socket.getOutputStream();
	}
	
	@Override
	public void onSendToClient(Message message) {
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> this.realSend(message));
	}
	
	private Void realSend(Message message) {
		try {
			transformer.send(message, out);
		} catch (JsonProcessingException e) {
			String error = "Error occured when serializing message " + message;
			logger.fatal(error, e);
			throw new IllegalArgumentException(error, e);
		} catch (IOException e) {
			String error = "Error occured when sending message " + message;
			logger.fatal(error, e);
//			this.disconnected(); // Possibly mark client as disconnected here
		}
		return null;
	}

	@Override
	public void run() {
		logger.info("Listening for messages using " + transformer);
		while (socket != null && socket.isConnected()) {
			try {
				transformer.read(in, mess -> incomingMess(mess));
			} catch (JsonParseException e) {
				this.sendToClient(new ServerErrorMessage("Error reading input: " + e.getMessage()));
				logger.error(e.getMessage(), e);
				this.close();
			} catch (JsonProcessingException e) {
				this.sendToClient(new ServerErrorMessage("Error processing input: " + e.getMessage()));
				logger.error(e.getMessage(), e);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				this.close();
			}
			if (Thread.interrupted()) {
				logger.info(this + " interrupted");
				close();
				break;
			}
			if (socket.isClosed()) {
				logger.info(this + " socket closed");
				close();
				break;
			}
		}
		logger.info("End of run method for " + this);
	}

	private boolean incomingMess(Message mess) {
		logger.info("Received from " + this + ": " + mess);
		if (mess instanceof TransformerMessage) {
			TransformerMessage transformMess = (TransformerMessage) mess;
			logger.info("Tranform mess " + transformMess.getType());
			switch (transformMess.getType()) {
				case TransformerMessage.TRANSFORM_JSON:
					this.transformer = new JsonSerialization(mapper);
					break;
				case TransformerMessage.TRANSFORM_BYTE:
					this.transformer = CardshifterIO.createByteTransformer();
					break;
				default:
					throw new IllegalArgumentException("Not a known transformer: " + transformMess.getType());
			}
			return false;
		}
		this.sentToServer(mess);
		return true;
	}

	@Override
	public void close() {
		this.disconnected();
		try {
			logger.info(this + " Closing socket");
			if (socket != null) {
				socket.close();
			}
		}
		catch (IOException e) {
			logger.warn("Error closing", e);
		}
		socket = null;
	}

	@Override
	public String getRemoteAddress() {
		if (socket == null) {
			return "Not connected";
		}
		return String.valueOf(socket.getRemoteSocketAddress());
	}
}
