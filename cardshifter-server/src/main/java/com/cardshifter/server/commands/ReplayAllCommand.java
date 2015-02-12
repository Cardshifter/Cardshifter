package com.cardshifter.server.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import org.apache.log4j.LogManager;

import com.cardshifter.core.game.FakeClient;
import com.cardshifter.core.game.TCGGame;
import com.cardshifter.core.replays.ReplayPlaybackSystem;
import com.cardshifter.core.replays.ReplayRecordSystem;
import com.cardshifter.modapi.base.ECSGameState;
import com.cardshifter.server.commands.ReplayAllCommand.ReplayAllParameters;
import com.cardshifter.server.model.CommandHandler.CommandHandle;
import com.cardshifter.server.model.Server;

public class ReplayAllCommand implements CommandHandle<ReplayAllParameters> {

	public static class ReplayAllParameters {
	}
	
	@Override
	public void handle(CommandContext command, ReplayAllParameters parameters) {
		File dir = new File(".");
		for (File file : dir.listFiles()) {
			if (!file.getName().startsWith("replay-")) {
				continue;
			}
			if (!file.getName().endsWith(".json")) {
				continue;
			}
			
			String replayMod = checkFile(command, file);
			command.sendChatResponse(file + " detected as " + replayMod);
		}
	}

	private String checkFile(CommandContext command, File file) {
		for (String mod : command.getServer().getGameFactories().keySet()) {
			try {
				if (checkReplay(command.getServer(), file, mod)) {
					return mod;
				}
			}
			catch (RuntimeException ex) {
				command.sendChatResponse("Error checking replay " + file + " with mod " + mod + ": " + ex);
				LogManager.getLogger(getClass()).error("Error checking replay " + file + " with mod " + mod, ex);
			}
		}
		return null;
	}

	private boolean checkReplay(Server server, File file, String mod) {
		ReplayRecordSystem replay;
		try {
			replay = CardshifterIO.mapper().readValue(file, ReplayRecordSystem.class);
		} catch (IOException e1) {
			throw new RuntimeException("Error loading replay: " + e1.getMessage(), e1);
		}
		
		String actualMod = replay.getModName() != null ? replay.getModName() : mod;
		TCGGame game = (TCGGame) server.createGame(actualMod);
		ReplayPlaybackSystem playback = new ReplayPlaybackSystem(game.getGameModel(), replay);
		game.getGameModel().addSystem(playback);
		FakeClient fake1 = new FakeClient(server, e -> {});
		FakeClient fake2 = new FakeClient(server, e -> {});
		game.start(Arrays.asList(fake1, fake2));
		System.out.println("Game state is " + game.getState());
		if (game.getState() == ECSGameState.NOT_STARTED) {
			System.out.println("Loading configs from saved data");
			playback.setPlayerConfigs(game.getGameModel());
			game.checkStartGame();
		}
	
		while (!playback.isReplayFinished()) {
			playback.nextStep();
		}
		
		return game.isGameOver();
	}

}
