package com.cardshifter.core.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.cardshifter.core.Game;

public class Main {

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		try (Scanner input = new Scanner(System.in)) {
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
			else {
				InputStream file = options.getScript() == null ? Game.class.getResourceAsStream("start.lua") : new FileInputStream(new File(options.getScript()));
				Game game = new Game(file, options.getRandom());
				game.getEvents().startGame(game);
				new ConsoleController(game).play(input);
			}
		}
	}
	
}
