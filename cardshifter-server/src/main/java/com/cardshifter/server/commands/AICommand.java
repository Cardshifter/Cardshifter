package com.cardshifter.server.commands;

import java.util.Optional;
import java.util.Set;

import net.zomis.cardshifter.ecs.ai.AIComponent;
import net.zomis.cardshifter.ecs.ai.AISystem;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.PlayerComponent;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.cardshifter.ai.IdleAI;
import com.cardshifter.server.commands.AICommand.AICommandParameters;
import com.cardshifter.server.main.FakeAIClientTCG;
import com.cardshifter.server.model.ClientIO;
import com.cardshifter.server.model.CommandHandler.CommandHandle;


public class AICommand implements CommandHandle<AICommandParameters> {
	
	@Parameters(commandNames = "ai", commandDescription = "Set the AI an entity uses in a game")
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
		
	}
	
	@Override
	public void handle(CommandContext command, AICommandParameters parameters) {
		ECSGame game = command.getServer().getGames().get(parameters.gameId).getGameModel();
		Set<Entity> players = game.getEntitiesWithComponent(PlayerComponent.class);
		Set<Entity> ais = game.getEntitiesWithComponent(AIComponent.class);
		players.stream().forEach(e -> command.sendChatResponse("Player " + e + ": " + e.getComponent(AIComponent.class)));
		ais.stream().forEach(e -> command.sendChatResponse("AI " + e + ": " + e.getComponent(AIComponent.class)));
		Entity entity = game.getEntity(parameters.entity);
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
		if (parameters.call) {
			command.sendChatResponse("Calling AIs in game...");
			AISystem.call(game);
		}
	}

	private AIComponent createIfNotExists(AIComponent obj, Entity entity) {
		if (obj == null) {
			AIComponent comp = new AIComponent(new IdleAI());
			entity.addComponent(comp);
			return comp;
		}
		return obj;
	}
	
}
