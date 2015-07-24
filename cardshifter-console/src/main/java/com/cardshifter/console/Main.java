package com.cardshifter.console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.cardshifter.core.game.ModCollection;

import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
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
			else {
                ModCollection modCollection = ModCollection.defaultMods();
                modCollection.loadExternal(modCollection.getDefaultModLocation());
                if (options.getMod() == null) {
                    System.out.println("Enter name of mod that you want to play: (" + modCollection.getAvailableMods() + ")");
                    options.setMod(input.nextLine());
                }

				ECSMod mod = modCollection.getModFor(options.getMod());
				ECSGame newgame = new ECSGame();
				mod.declareConfiguration(newgame);
				mod.setupGame(newgame);
				new ConsoleControllerECS(newgame).play(input);
			}
		}
	}

}
