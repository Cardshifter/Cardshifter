package com.cardshifter.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.log4j.PropertyConfigurator;

import net.zomis.cardshifter.ecs.main.ConsoleControllerECS;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.cardshifter.core.Game;

public class Main {

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		PropertyConfigurator.configure(Main.class.getResourceAsStream("log4j.properties"));
		try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
			CommandLineOptions options = new CommandLineOptions();
			JCommander jcommander = new JCommander(options);
			try {
				jcommander.parse(args);
			}
			catch (ParameterException ex) {
				System.out.println(ex.getMessage());
				jcommander.usage();
				return;
			}
			
			if (options.getHost() != null) {
				NetworkConsoleController networkController = new NetworkConsoleController(options.getHost(), options.getPort());
				networkController.play(input);
			}
			else if (options.isLua()) {
				startLuaGame(options);
			}
			else {
				new ConsoleControllerECS(PhrancisGame.createGame()).play(input);
			}
		}
	}

	@Deprecated
	private static void startLuaGame(CommandLineOptions options) throws FileNotFoundException {
		InputStream file = options.getScript() == null ? Main.class.getResourceAsStream("/com/cardshifter/mod/start.lua") : new FileInputStream(new File(options.getScript()));
		Game game = new Game(file, options.getRandom());
		game.getEvents().startGame(game);
		new ConsoleController(game).play(new Scanner(System.in, StandardCharsets.UTF_8.name()));
	}
	
}
