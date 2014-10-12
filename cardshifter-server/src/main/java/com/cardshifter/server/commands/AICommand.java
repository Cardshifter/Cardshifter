package com.cardshifter.server.commands;

import java.util.Optional;
import java.util.Set;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.cardshifter.ai.IdleAI;
import com.cardshifter.modapi.ai.AIComponent;
import com.cardshifter.modapi.ai.AISystem;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.server.commands.AICommand.AICommandParameters;
import com.cardshifter.server.main.FakeAIClientTCG;
import com.cardshifter.server.model.ClientIO;
import com.cardshifter.server.model.CommandHandler.CommandHandle;


public class AICommand implements CommandHandle<AICommandParameters> {
	
	@Parameters(commandNames = "ai", commandDescription = "Control AIs within a game")
	public static class AICommandParameters {
		@Parameter(names = "-g", description = "Gameid", required = true)
		private int gameId;
		
		@Parameter(names = "-e", description = "Entity ID")
		private int entity;
		
		@Parameter(names = "-delay", description = "Change the delay")
		private int delay;
		
		@Parameter(names = "-ai", description = "Change the AI")
		private String aiName;
		
		@Parameter(names = "-call", description = "Call the AI")
		private boolean call;
		
		@Parameter(names = "pause", description = "Pause all AIs in the game")
		private boolean pause;
		
		@Parameter(names = "continue", description = "Unpause all AIs in the game")
		private boolean cont;
		
	}
	
	@Override
	public void handle(CommandContext command, AICommandParameters parameters) {
		ECSGame game = command.getServer().getGames().get(parameters.gameId).getGameModel();
		Set<Entity> players = game.getEntitiesWithComponent(PlayerComponent.class);
		Set<Entity> ais = game.getEntitiesWithComponent(AIComponent.class);
		players.stream().forEach(e -> command.sendChatResponse("Player " + e + ": " + e.getComponent(AIComponent.class)));
		ais.stream().forEach(e -> command.sendChatResponse("AI " + e + ": " + e.getComponent(AIComponent.class)));
		Entity entity = game.getEntity(parameters.entity);
		
		if (parameters.call) {
			command.sendChatResponse("Calling AIs in game...");
			AISystem.call(game);
			return;
		}
		
		if (parameters.pause) {
			ais.forEach(e -> e.getComponent(AIComponent.class).setPaused(true));
			return;
		}
		
		if (parameters.cont) {
			ais.forEach(e -> e.getComponent(AIComponent.class).setPaused(false));
			return;
		}
		
		if (entity == null) {
			command.sendChatResponse("No entity specified");
			return;
		}
		
		AIComponent ai = entity.getComponent(AIComponent.class);
		
		command.sendChatResponse("Chosen entity is " + entity);
		if (parameters.aiName != null) {
			parameters.aiName = parameters.aiName.replace("_", " ");
			Optional<ClientIO> client = command.getServer().getClients().values().stream().filter(cl -> cl.getName().equals(parameters.aiName)).findAny();
			if (client.isPresent()) {
				FakeAIClientTCG aiClient = (FakeAIClientTCG) client.get();
				ai = createIfNotExists(ai, entity);
				ai.setAI(aiClient.getAI());
				command.sendChatResponse("Changing AI to " + aiClient.getAI());
			}
			else {
				entity.removeComponent(AIComponent.class);
				command.sendChatResponse("Removing AI");
			}
		}
		if (parameters.delay != 0) {
			ai = createIfNotExists(ai, entity);
			ai.setDelay(parameters.delay);
			command.sendChatResponse("Changing delay to " + parameters.delay);
		}
	}

	private AIComponent createIfNotExists(AIComponent obj, Entity entity) {
		if (obj == null) {
			AIComponent comp = new AIComponent(new IdleAI());
			comp.setDelay(10000);
			entity.addComponent(comp);
			return comp;
		}
		return obj;
	}
	
}
