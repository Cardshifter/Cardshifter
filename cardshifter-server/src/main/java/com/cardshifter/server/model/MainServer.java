package com.cardshifter.server.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.SimpleGame;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.api.CardshifterConstants;
import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.modapi.ai.CardshifterAI;
import com.cardshifter.server.commands.AICommand;
import com.cardshifter.server.commands.HelpCommand;
import com.cardshifter.server.commands.AICommand.AICommandParameters;
import com.cardshifter.server.commands.HelpCommand.HelpParameters;
import com.cardshifter.server.commands.CommandContext;
import com.cardshifter.server.main.FakeAIClientTCG;
import com.cardshifter.server.utils.export.DataExporter;

public class MainServer {
	
	private static final Logger logger = LogManager.getLogger(MainServer.class);
	
	private final Server server = new Server();
	private final Map<String, CardshifterAI> ais = new LinkedHashMap<>();

	private Thread consoleThread;
	
	public Server start() {
		ais.put("Loser", new ScoringAI(AIs.loser()));
		ais.put("Idiot", new ScoringAI(AIs.idiot()));
		ais.put("Medium", new ScoringAI(AIs.medium()));
		ais.put("Fighter", new ScoringAI(AIs.fighter()));
		
		try {
			logger.info("Starting Server...");
			
			server.addConnections(new ServerSock(server, 4242));
			server.addConnections(new ServerWeb(server, 4243));
			
			logger.info("Starting Console...");
			CommandHandler commandHandler = server.getCommandHandler();
			initializeCommands(commandHandler);
			ServerConsole console = new ServerConsole(server, commandHandler);
			consoleThread = new Thread(console, "Console-Thread");
			consoleThread.start();
			
			ais.entrySet().forEach(entry -> {
				ClientIO tcgAI = new FakeAIClientTCG(server, entry.getValue());
				server.newClient(tcgAI);
				server.getIncomingHandler().perform(new LoginMessage("AI " + entry.getKey()), tcgAI);
			});
			
			server.addGameFactory(CardshifterConstants.VANILLA, (serv, id) -> new TCGGame(serv, id, new PhrancisGame()));
			server.addGameFactory("SIMPLE", (serv, id) -> new TCGGame(serv, id, new SimpleGame()));
			
			logger.info("Started");
		}
		catch (Exception e) {
			logger.error("Initializing Error", e);
		}
		return server;
	}
	
	private void initializeCommands(CommandHandler commandHandler) {
		commandHandler.addHandler("exit", () -> new Object(), this::shutdown);
		commandHandler.addHandler("help", () -> new HelpParameters(), new HelpCommand(commandHandler));
		commandHandler.addHandler("export", this::export);
		commandHandler.addHandler("users", this::users);
		commandHandler.addHandler("play", this::play);
		commandHandler.addHandler("say", this::say);
		commandHandler.addHandler("chat", this::chatInfo);
		commandHandler.addHandler("games", this::showGames);
		commandHandler.addHandler("invites", this::showInvites);
		commandHandler.addHandler("ai", () -> new AICommandParameters(), new AICommand());
		commandHandler.addHandler("threads", cmd -> showAllStackTraces(server, System.out::println));
	}
	
	private void showInvites(Command command) {
		for (Entry<Integer, GameInvite> ee : server.getInvites().all().entrySet()) {
			System.out.println(ee.getKey() + " = " + ee.getValue());
		}
	}
	
	private void showGames(Command command) {
		for (Entry<Integer, ServerGame> ee : server.getGames().entrySet()) {
			System.out.println(ee.getKey() + " = " + ee.getValue());
		}
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
	
	private void shutdown(CommandContext command, Object parameters) {
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
		server.getClients().values().forEach(cl -> System.out.println(cl));
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
