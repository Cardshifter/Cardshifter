package com.cardshifter.client;

import com.cardshifter.server.incoming.LoginMessage;
import com.cardshifter.server.incoming.RequestTargetsMessage;
import com.cardshifter.server.incoming.StartGameRequest;
import com.cardshifter.server.incoming.UseAbilityMessage;
import com.cardshifter.server.messages.Message;
import com.cardshifter.server.outgoing.NewGameMessage;
import com.cardshifter.server.outgoing.ResetAvailableActionsMessage;
import com.cardshifter.server.outgoing.UseableActionMessage;
import com.cardshifter.server.outgoing.WaitMessage;
import com.cardshifter.server.outgoing.WelcomeMessage;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
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
		
		try {
			this.play(new Scanner(System.in));
		} catch (Exception e) {
			
		}
	}
	
	public void play(Scanner input) throws IOException, InterruptedException {
		System.out.println("Enter your name: ");
//		String name = input.nextLine();
		String name = "Player" + new Random().nextInt(100);
		this.send(new LoginMessage(name));
		
		WelcomeMessage response = (WelcomeMessage) messages.take();
		System.out.println(response.getMessage());
		if (!response.isOK()) {
			return;
		}
		
		this.send(new StartGameRequest());
		Message message = messages.take();
		if (message instanceof WaitMessage) {
			System.out.println(((WaitMessage) message).getMessage());
			NewGameMessage game = (NewGameMessage) messages.take();
			this.playLoop(game, input);
		}
		else {
			this.playLoop((NewGameMessage) message, input);
		}
	}
	
	private void playLoop(NewGameMessage game, Scanner input) throws JsonParseException, JsonMappingException, IOException {
		System.out.printf("Game id %d. You are player index %d.%n", game.getGameId(), game.getPlayerIndex());
		this.gameId = game.getGameId();
		while (true) {
			System.out.println("Waiting for input...");
			
			// TODO: Empty messages, perhaps do something with some of them as well...
//			try {
//				while (true) {
//					// 
//					messages.poll(500, TimeUnit.MILLISECONDS);
//				}
//			} catch (InterruptedException e) {
//			}
			
			outputList(actions);
			if (actions.isEmpty()) {
				System.out.println("No available actions to perform right now.");
			}
			
			String inputLine = input.nextLine();
			if (inputLine.equals("exit")) {
				break;
			}
			
			try {
				int actionIndex = Integer.parseInt(inputLine);
				UseableActionMessage action = actions.get(actionIndex);
				if (action.isTargetRequired()) {
					this.send(new RequestTargetsMessage(gameId, action.getId(), action.getAction()));
				}
				else {
					this.send(new UseAbilityMessage(gameId, action.getId(), action.getAction(), action.getTargetId()));
				}
			}
			catch (NumberFormatException | IndexOutOfBoundsException ex) {
				System.out.println("Not a valid action");
			}
		}
		//print("--------------------------------------------");
		//print("Game over!");
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
	
	private void send(Message message) {
		try {
			System.out.println("Sending: " + this.mapper.writeValueAsString(message));
			this.mapper.writeValue(out, message);
		} catch (IOException e) {
			System.out.println("Error sending message: " + message);
			throw new RuntimeException(e);
		}
	}

	private void outputList(final List<?> actions) {
		Objects.requireNonNull(actions, "actions");
		//print("------------------");
		ListIterator<?> it = actions.listIterator();
		while (it.hasNext()) {
			//print(it.nextIndex() + ": " + it.next());
		}
	}
}
