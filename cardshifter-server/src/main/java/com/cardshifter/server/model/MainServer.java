package com.cardshifter.server.model;

import java.util.Map;
import java.util.function.Consumer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.incoming.LoginMessage;
import com.cardshifter.server.incoming.StartGameRequest;
import com.cardshifter.server.main.FakeAIClientTCG;

public class MainServer {
	private static final Logger logger = LogManager.getLogger(MainServer.class);
	
	public void start() {
		try {
			logger.info("Starting Server...");
			Server server = new Server();
			
			server.addConnections(new ServerSock(server, 4242));
			server.addConnections(new ServerWeb(server, 4243));
			
			logger.info("Starting Console...");
			CommandHandler commandHandler = new CommandHandler();
			commandHandler.addHandler("exit", command -> System.exit(0));
			ServerConsole console = new ServerConsole(server, commandHandler);
			new Thread(console, "Console-Thread").start();
			console.addHandler("threads", cmd -> showAllStackTraces(server, System.out::println));
			logger.info("Started");
			
			// Setup an AI that automatically wants to play (for testing purposes)
			ClientIO tcgAI = new FakeAIClientTCG(server);
			server.newClient(tcgAI);
			server.getIncomingHandler().perform(new LoginMessage("AI Simple"), tcgAI);
			server.getIncomingHandler().perform(new StartGameRequest(), tcgAI);
		}
		catch (Exception e) {
			logger.error("Initializing Error", e);
		}
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
