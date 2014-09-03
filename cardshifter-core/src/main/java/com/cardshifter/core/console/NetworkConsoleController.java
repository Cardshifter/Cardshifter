package com.cardshifter.core.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

import com.cardshifter.core.TargetAction;
import com.cardshifter.core.Targetable;
import com.cardshifter.core.UsableAction;
import com.cardshifter.server.incoming.LoginMessage;
import com.cardshifter.server.incoming.StartGameRequest;
import com.cardshifter.server.messages.Message;
import com.cardshifter.server.outgoing.EndOfSequenceMessage;
import com.cardshifter.server.outgoing.NewGameMessage;
import com.cardshifter.server.outgoing.WaitMessage;
import com.cardshifter.server.outgoing.WelcomeMessage;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NetworkConsoleController {
	private final Socket socket;
	private InputStream in;
	private OutputStream out;
	private final ObjectMapper mapper = new ObjectMapper();
	
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
		while (true) {
			Message mess;
			do {
				mess = receive(Message.class);
//				System.out.println(mess);
			}
			while (!(mess instanceof EndOfSequenceMessage));
			
			
			System.out.println("Retrieve game state...");
			
//			List<UsableAction> actions = game.getAllActions().stream().filter(action -> action.isAllowed()).collect(Collectors.toList());
//			outputList(actions);
//			
			String in = input.nextLine();
			if (in.equals("exit")) {
				break;
			}
			
//			handleActionInput(actions, in, input);
		}
		print("--------------------------------------------");
		print("Game over!");
	}


	private <T> T receive(Class<T> class1) throws JsonParseException, JsonMappingException, IOException {
//		String value = IOUtils.toString(in);
//		System.out.println("Reading: " + value);
		T mapp = mapper.readValue(in, class1);
		System.out.println("Received: " + mapper.writeValueAsString(mapp));
		return mapp;
	}


	private void send(Message message) {
		try {
			this.mapper.writeValue(out, message);
		} catch (IOException e) {
			System.out.println("Error sending message: " + message);
			throw new RuntimeException(e);
		}
	}


	private void handleActionInput(final List<UsableAction> actions, final String in, Scanner input) {
		Objects.requireNonNull(actions, "actions");
		Objects.requireNonNull(in, "in");
		print("Choose an action:");
		
		try {
			int value = Integer.parseInt(in);
			if (value < 0 || value >= actions.size()) {
				print("Action index out of range: " + value);
				return;
			}
			
			UsableAction action = actions.get(value);
			print("Action " + action);
			if (action.isAllowed()) {
				if (action instanceof TargetAction) {
					TargetAction targetAction = (TargetAction) action;
					List<Targetable> targets = targetAction.findTargets();
					if (targets.isEmpty()) {
						print("No available targets for action");
						return;
					}
					
					outputList(targets);
					print("Enter target index:");
					int targetIndex = Integer.parseInt(input.nextLine());
					if (value < 0 || value >= actions.size()) {
						print("Target index out of range: " + value);
						return;
					}
					
					Targetable target = targets.get(targetIndex);
					targetAction.perform(target);
				}
				else action.perform();
				print("Action performed");
			}
			else {
				print("Action is not allowed");
			}
		}
		catch (NumberFormatException ex) {
			print("Illegal action index: " + in);
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
