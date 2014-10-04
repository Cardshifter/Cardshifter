package com.cardshifter.server.clients;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.ServerErrorMessage;
import com.cardshifter.server.model.ClientIO;
import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientSocketHandler extends ClientIO implements Runnable {
	private static final Logger logger = LogManager.getLogger(ClientSocketHandler.class);
	
	private Socket	socket;
	private final InputStream in;
	@Deprecated
	private final PrintWriter out; // TODO: Convert to simple OutputStream and let Jackson do the job
	
	private final ObjectMapper mapper = new ObjectMapper();

	public ClientSocketHandler(Server server, Socket socket) throws IOException {
		super(server);
		this.socket = socket;
		in = socket.getInputStream();
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
	}
	
	@Override
	public void onSendToClient(String message) {
		this.out.print(message);
		this.out.flush();
	}

	@Override
	public void run() {
		while (socket != null && socket.isConnected()) {
			try {
				MappingIterator<Message> values;
				values = mapper.readValues(new JsonFactory().createParser(this.in), Message.class);
				while (values.hasNextValue()) {
					Message message = values.next();
					logger.info("Received from " + this + ": " + message);
					this.sentToServer(message);
				}
			} catch (JsonParseException e) {
				this.sendToClient(new ServerErrorMessage("Error reading input: " + e.getMessage()));
				logger.error(e.getMessage(), e);
			} catch (JsonProcessingException e) {
				this.sendToClient(new ServerErrorMessage("Error processing input: " + e.getMessage()));
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				this.disconnected();
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e1) {
					}
				}
				socket = null;
				logger.error(e.getMessage(), e);
			}
		}
		logger.info("End of run method for " + this);
	}

	@Override
	public void close() {
		try {
			socket.close();
		}
		catch (IOException e) {
			logger.warn("Error closing", e);
		}
		this.disconnected();
	}
}
