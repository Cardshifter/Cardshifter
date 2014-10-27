package com.cardshifter.server.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import com.beust.jcommander.Parameter;
import com.cardshifter.core.game.FakeClient;
import com.cardshifter.core.game.TCGGame;
import com.cardshifter.core.replays.ReplayPlaybackSystem;
import com.cardshifter.core.replays.ReplayRecordSystem;
import com.cardshifter.modapi.base.ECSGameState;
import com.cardshifter.server.commands.ReplayCommand.ReplayParameters;
import com.cardshifter.server.model.CommandHandler.CommandHandle;
import com.cardshifter.server.model.GameFactory;

public class ReplayCommand implements CommandHandle<ReplayParameters> {

	public static class ReplayParameters {
		@Parameter(names = "--file")
		private String file;
		
		@Parameter(names = "--mod")
		private String mod;
	}
	
	@Override
	public void handle(CommandContext command, ReplayParameters parameters) {
		GameFactory factory = command.getServer().getGameFactories().get(parameters.mod);
		if (factory == null) {
			command.sendChatResponse("Invalid mod: " + parameters.mod);
			return;
		}
		
		File file = new File(parameters.file);
		if (!file.exists()) {
			command.sendChatResponse("File does not exist: " + file);
			return;
		}
		
		TCGGame game = (TCGGame) command.getServer().createGame(parameters.mod);
		ReplayRecordSystem replay;
		try {
			replay = CardshifterIO.mapper().readValue(file, ReplayRecordSystem.class);
		} catch (IOException e1) {
			throw new RuntimeException("Error loading replay: " + e1.getMessage(), e1);
		}
		
		ReplayPlaybackSystem playback = new ReplayPlaybackSystem(game.getGameModel(), replay);
		game.getGameModel().addSystem(playback);
		game.start(Arrays.asList(command.getClient(), new FakeClient(command.getServer(), e -> {})));
		System.out.println("Game state is " + game.getState());
		if (game.getState() == ECSGameState.NOT_STARTED) {
			System.out.println("Loading configs from saved data");
			playback.setPlayerConfigs(game.getGameModel());
			game.checkStartGame();
		}
		
		
	}

}
