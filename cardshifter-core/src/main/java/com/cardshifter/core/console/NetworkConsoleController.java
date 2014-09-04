package com.cardshifter.core.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

import com.cardshifter.server.incoming.LoginMessage;
import com.cardshifter.server.incoming.StartGameRequest;
import com.cardshifter.server.incoming.UseAbilityMessage;
import com.cardshifter.server.messages.Message;
import com.cardshifter.server.outgoing.EndOfSequenceMessage;
import com.cardshifter.server.outgoing.NewGameMessage;
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

public class NetworkConsoleController {
	private final Socket socket;
	private InputStream in;
	private OutputStream out;
	private final ObjectMapper mapper = new ObjectMapper();
	private int gameId;
	
	public NetworkConsoleController(String hostname, int port) throws UnknownHostException, IOException {
		socket = new Socket(hostname, port);
		out = socket.getOutputStream();
		in = socket.getInputStream();
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
//		mapper.reader().readValues(in);
	}
	
	
	public void play(Scanner input) throws IOException {
		System.out.println("Enter your name: ");
//		String name = input.nextLine();
		String name = "Player" + new Random().nextInt(100);
		this.send(new LoginMessage(name));
		
		WelcomeMessage response = receive(WelcomeMessage.class);
		System.out.println(response.getMessage());
		if (!response.isOK()) {
			return;
		}
		
		this.send(new StartGameRequest());
		Message message = receive(Message.class);
		if (message instanceof WaitMessage) {
			System.out.println(((WaitMessage) message).getMessage());
			NewGameMessage game = receive(NewGameMessage.class);
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
			Message mess = null;
			List<UseableActionMessage> actions = new ArrayList<>();
			do {
				System.out.println("Start loop");
				
				MappingIterator<Message> values = mapper.readValues(new JsonFactory().createParser(in), Message.class);
				while (values.hasNext()) {
					mess = values.next();
					System.out.println("iterator: " + mess);
					if (mess instanceof EndOfSequenceMessage) {
						break;
					}
					if (mess instanceof UseableActionMessage) {
						actions.add((UseableActionMessage) mess);
					}
				}
				System.out.println("End of loop, mess is " + mess);
			}
			while (!(mess instanceof EndOfSequenceMessage));
			
			
			System.out.println("Waiting for input...");
			
			outputList(actions);
			if (actions.isEmpty()) {
				System.out.println("No available actions to perform right now.");
			}
			
			String in = input.nextLine();
			if (in.equals("exit")) {
				break;
			}
			
			try {
				int actionIndex = Integer.parseInt(in);
				UseableActionMessage action = actions.get(actionIndex);
				this.send(new UseAbilityMessage(gameId, action.getId(), action.getAction()));
			}
			catch (NumberFormatException ex) {
				System.out.println("Not a valid action");
			}
		}
		print("--------------------------------------------");
		print("Game over!");
	}

	private <T> T receive(Class<T> class1) throws JsonParseException, JsonMappingException, IOException {
		T mapp = mapper.readValue(in, class1);
		System.out.println("Received: " + mapper.writeValueAsString(mapp));
		return mapp;
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
		print("------------------");
		ListIterator<?> it = actions.listIterator();
		while (it.hasNext()) {
			print(it.nextIndex() + ": " + it.next());
		}
	}

	private void print(final Object object) {
		print(0, object);
	}
	
	private void print(final int indentation, final Object object) {
		System.out.println(indent(indentation) + object.toString());
	}
	
	private String indent(final int amount) {
		if (amount == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(amount);
		for (int i = 0; i < amount; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		NetworkConsoleController control = new NetworkConsoleController("127.0.0.1", 4242);
		control.play(new Scanner(System.in));
	}
	
}
