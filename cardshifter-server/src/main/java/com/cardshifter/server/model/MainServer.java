package com.cardshifter.server.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.CardshifterAI;
import com.cardshifter.ai.CompleteIdiot;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.api.CardshifterConstants;
import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.server.main.FakeAIClientTCG;
import com.cardshifter.server.utils.export.DataExporter;

public class MainServer {
	
	private static final Logger logger = LogManager.getLogger(MainServer.class);
	
	private final Server server = new Server();
	private final Map<String, CardshifterAI> ais = new LinkedHashMap<>();

	private Thread consoleThread;
	
	public Server start() {
		ais.put("loser", new ScoringAI(AIs.loser()));
		ais.put("idiot", new ScoringAI(AIs.idiot()));
		ais.put("old", new CompleteIdiot());
		ais.put("medium", new ScoringAI(AIs.medium()));
		
		try {
			logger.info("Starting Server...");
			
			server.addConnections(new ServerSock(server, 4242));
			server.addConnections(new ServerWeb(server, 4243));
			
			logger.info("Starting Console...");
			CommandHandler commandHandler = new CommandHandler();
			commandHandler.addHandler("exit", command -> this.shutdown());
			commandHandler.addHandler("export", this::export);
			commandHandler.addHandler("users", this::users);
			commandHandler.addHandler("play", this::play);
			commandHandler.addHandler("say", this::say);
			commandHandler.addHandler("chat", this::chatInfo);
			ServerConsole console = new ServerConsole(server, commandHandler);
			consoleThread = new Thread(console, "Console-Thread");
			consoleThread.start();
			console.addHandler("threads", cmd -> showAllStackTraces(server, System.out::println));
			
			ais.entrySet().forEach(entry -> {
				ClientIO tcgAI = new FakeAIClientTCG(server, entry.getValue());
				server.newClient(tcgAI);
				server.getIncomingHandler().perform(new LoginMessage("AI " + entry.getKey()), tcgAI);
			});
			
			logger.info("Started");
		}
		catch (Exception e) {
			logger.error("Initializing Error", e);
		}
		return server;
	}
	
	private void say(Command command) {
		ChatArea chat = server.getMainChat();
		chat.broadcast(new ChatMessage(chat.getId(), "Server", command.getFullCommand(1)));
	}
	
	private void chatInfo(Command command) {
		int chatId = command.getParameterInt(1);
		if (chatId == 0) {
			System.out.println(server.getChats().keySet());
		}
		else {
			ChatArea chat = server.getMainChat();
			System.out.println(chat.getUsers());
		}
	}
	
	private void shutdown() {
		server.stop();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			System.out.println("Interrupted when shutting down");
		}
		
		showAllStackTraces(server, System.out::println);
		consoleThread.interrupt();
	}
	
	private void users(Command command) {
		server.getClients().values().forEach(cl -> System.out.println(cl.getId() + ": " + cl.getName()));
	}
	
	private void play(Command command) {
		int userId = command.getParameterInt(1);
		ClientIO client = server.getClients().get(userId);
		server.getIncomingHandler().perform(new StartGameRequest(-1, CardshifterConstants.VANILLA), client);
	}
	
	private void export(Command command) {
		server.createGame(CardshifterConstants.VANILLA);
		DataExporter exporter = new DataExporter();
		exporter.export(server, command.getAllParameters());
	}
	
	private void showAllStackTraces(Server server, Consumer<String> output) {
		output.accept("All stack traces:");
		Map<Thread, StackTraceElement[]> allTraces = Thread.getAllStackTraces();
		for (Thread thread : allTraces.keySet()) {
			output.accept(thread.getName());
			this.stackTrace(thread, output);
		}
	}
	
	private void stackTrace(Thread thread, Consumer<String> output) {
		StackTraceElement[] stackTrace = thread.getStackTrace();
		output.accept("Stack trace for thread " + thread.getId() + ": " + thread.getName());
		for (StackTraceElement trace : stackTrace) {
			output.accept(trace.toString());
		}
		output.accept("");
	}
	
}
