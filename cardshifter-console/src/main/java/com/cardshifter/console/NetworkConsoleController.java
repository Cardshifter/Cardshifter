package com.cardshifter.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import com.cardshifter.api.CardshifterConstants;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.RequestTargetsMessage;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.api.outgoing.ResetAvailableActionsMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.api.outgoing.WaitMessage;
import com.cardshifter.api.outgoing.WelcomeMessage;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NetworkConsoleController {
	private final Socket socket;
	private final InputStream in;
	private final OutputStream out;
	private final ObjectMapper mapper = CardshifterIO.mapper();
	private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
	private final List<UsableActionMessage> actions = Collections.synchronizedList(new ArrayList<>());
	private int gameId;
	
	public NetworkConsoleController(String hostname, int port) throws UnknownHostException, IOException {
		socket = new Socket(hostname, port);
		out = socket.getOutputStream();
		in = socket.getInputStream();
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		initThreads();
	}
	
	private void initThreads() {
		new Thread(this::listen).start();
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
		
		this.send(new StartGameRequest(-1, CardshifterConstants.VANILLA));
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
	
	private void listen() {
		while (true) {
			try {
				Message mess = null;
				System.out.println("Start loop");
				MappingIterator<Message> values = mapper.readValues(new JsonFactory().createParser(in), Message.class);
				while (values.hasNext()) {
					mess = values.next();
					System.out.println("iterator: " + mess);
					try {
						messages.put(mess);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
					if (mess instanceof ResetAvailableActionsMessage) {
						actions.clear();
					}
					if (mess instanceof UsableActionMessage) {
						System.out.println("New Action Available: " + actions.size() + " - " + mess);
						actions.add((UsableActionMessage) mess);
					}
				}
				System.out.println("End of loop, mess is " + mess);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
				UsableActionMessage action = actions.get(actionIndex);
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
		print("--------------------------------------------");
		print("Game over!");
	}

//	@Deprecated
//	private <T> T receive(Class<T> class1) throws JsonParseException, JsonMappingException, IOException {
//		T mapp = mapper.readValue(in, class1);
//		System.out.println("Received: " + mapper.writeValueAsString(mapp));
//		return mapp;
//	}

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
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		NetworkConsoleController control = new NetworkConsoleController("127.0.0.1", 4242);
		control.play(new Scanner(System.in, StandardCharsets.UTF_8.name()));
	}
	
}
