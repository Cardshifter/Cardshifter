package com.cardshifter.server.commands;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.zomis.aiscores.FieldScore;
import net.zomis.aiscores.FieldScores;
import net.zomis.aiscores.Scorer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.cardshifter.ai.IdleAI;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.api.ClientIO;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.ai.AIComponent;
import com.cardshifter.modapi.ai.AISystem;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.server.commands.AICommand.AICommandParameters;
import com.cardshifter.server.main.FakeAIClientTCG;
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
		
		@Parameter(names = "-pause", description = "Pause all AIs in the game")
		private boolean pause;
		
		@Parameter(names = "-continue", description = "Unpause all AIs in the game")
		private boolean cont;
		
		@Parameter(names = "-score", description = "Score (Useful for AI debugging)")
		private boolean score;
		
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
			command.sendChatResponse("AIs paused");
			ais.forEach(e -> e.getComponent(AIComponent.class).setPaused(true));
			return;
		}
		
		if (parameters.cont) {
			command.sendChatResponse("AIs unpaused");
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
		
		if (parameters.score) {
			ai = createIfNotExists(ai, entity);
			if (ai.getAI() instanceof ScoringAI) {
				ScoringAI scorer = (ScoringAI) ai.getAI();
				outputScore(command, scorer, entity);
			}
		}
	}

	private void outputScore(CommandContext command, ScoringAI scorer, Entity entity) {
		FieldScores<Entity, ECSAction> results = scorer.calculateFullScore(entity);
		
		List<FieldScore<ECSAction>> scores = results.getScores().values().stream()
			.sorted(Comparator.<FieldScore<ECSAction>>comparingDouble(key -> key.getScore()).reversed())
			.collect(Collectors.toList());
		
		for (FieldScore<ECSAction> score : scores) {
			command.sendChatResponse("Scored field: " + score.getField() + " rank " + score.getRank() + " / " + results.getRankCount());
			
			Map<Scorer, Double> detailed = score.getScoreMap();
			for (Entry<Scorer, Double> ee : detailed.entrySet()) {
				command.sendChatResponse(ee.getKey() + ": " + ee.getValue());
			}
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
