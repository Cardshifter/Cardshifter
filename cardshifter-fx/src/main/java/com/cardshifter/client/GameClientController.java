package com.cardshifter.client;

import com.cardshifter.server.messages.Message;
import com.cardshifter.server.outgoing.ResetAvailableActionsMessage;
import com.cardshifter.server.outgoing.UseableActionMessage;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameClientController {
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private final ObjectMapper mapper = new ObjectMapper();
	private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	private final List<UseableActionMessage> actions = Collections.synchronizedList(new ArrayList<>());
	private int gameId;
	
	private String ipAddress;
	private int port;
	
	public void acceptIPAndPort(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public void connectToGame() {
		try {
			this.socket = new Socket(this.ipAddress, this.port);
			this.out = socket.getOutputStream();
			this.in = socket.getInputStream();
			mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
			mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
			new Thread(this::listen).start();
		} catch (Exception e) {

		}
	}
	
	private void listen() {
		while (true) {
			try {
				Message mess = null;
				System.out.println("Start loop");
				MappingIterator<Message> values = mapper.readValues(new JsonFactory().createParser(this.in), Message.class);
				while (values.hasNext()) {
					mess = values.next();
					System.out.println("iterator: " + mess);
					messages.offer(mess);
					if (mess instanceof ResetAvailableActionsMessage) {
						actions.clear();
					}
					if (mess instanceof UseableActionMessage) {
						System.out.println("New Action Available: " + actions.size() + " - " + mess);
						actions.add((UseableActionMessage) mess);
					}
				}
				System.out.println("End of loop, mess is " + mess);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
