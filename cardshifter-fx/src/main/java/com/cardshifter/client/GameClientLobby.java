/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cardshifter.client;

import com.cardshifter.api.messages.Message;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;

/**
 *
 * @author baz
 */
public class GameClientLobby {
	
	private final ObjectMapper mapper = new ObjectMapper();
	private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	private Socket socket;	
	private InputStream in;
	private OutputStream out;
	private String ipAddress;
	private int port;
	
	private Thread listenThread;
	private Thread playThread;
	
	public void acceptIPAndPort(String ipAddress, int port) {
		// this is passed into this object after it is automatically created by the FXML document
		this.ipAddress = ipAddress;
		this.port = port;
	}
	public boolean connectToGame() {
		// this is called on the object from the Game launcher before the scene is displayed
		try {
			this.socket = new Socket(this.ipAddress, this.port);
			this.out = socket.getOutputStream();
			this.in = socket.getInputStream();
			mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
			mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
			this.listenThread = new Thread(this::listen);
			this.listenThread.start();
		} catch (IOException ex) {
			System.out.println("Connection Failed");
			return false;
		}
		//this.playThread = new Thread(this::play);
		//this.playThread.start();	
		return true;
	}
	
	private void listen() {
		while (true) {
			try {
				MappingIterator<Message> values = mapper.readValues(new JsonFactory().createParser(this.in), Message.class);
				while (values.hasNextValue()) {
					Message message = values.next();
					try {
						messages.put(message);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
					//This is where all the magic happens for message handling
					Platform.runLater(() -> this.processMessageFromServer(message));
				}
			} catch (SocketException e) {
				//Platform.runLater(() -> loginMessage.setText(e.getMessage()));
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void processMessageFromServer(Message message) {	
		//this is for diagnostics so I can copy paste the messages to know their format
		System.out.println(message.toString());
	}
	
}
