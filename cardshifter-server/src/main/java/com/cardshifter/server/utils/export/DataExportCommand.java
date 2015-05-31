package com.cardshifter.server.utils.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.cardshifter.server.commands.CommandContext;
import com.cardshifter.server.model.CommandHandler;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import com.cardshifter.core.game.ServerGame;
import com.cardshifter.core.game.TCGGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class DataExportCommand implements CommandHandler.CommandHandle<DataExportCommand.DataExportParameters> {

    @Parameters(commandDescription = "Export the cards of the game id specified with -game")
    public static class DataExportParameters {
//		/export -game 2

        @Parameter(names = "-game", description = "Gameid")
        public int gameId;

    }

    @Override
    public void handle(CommandContext command, DataExportParameters parameters) {
        ServerGame game = command.getServer().getGames().get(parameters.gameId);
        if (game == null) {
            System.out.println("Invalid game");
            return;
        }

        ObjectMapper mapper = createMapper();

        List<Entity> list = new ArrayList<>();
        gatherInterestingEntities(list, game);

        File file = new File("game-" + game.getId() + ".json");
        try {
            mapper.writeValue(file, list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        command.sendChatResponse("Exported " + list.size() + " cards to " + file.getName());
    }

	private void gatherInterestingEntities(List<Entity> list, ServerGame game) {
		TCGGame g = (TCGGame) game;
		
		List<Entity> zone = new ArrayList<>(g.getGameModel().getEntitiesWithComponent(ZoneComponent.class));
		zone.sort(Comparator.comparing(e -> e.getId()));
		Entity entity = zone.get(0);
		Stream<Entity> stream = entity.getSuperComponents(ZoneComponent.class).stream().flatMap(z -> z.stream());
		
		List<Entity> entities = stream.collect(Collectors.toList());
		System.out.println("Found " + entities.size() + " interesting entities to save");
		list.addAll(entities);
	}

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = CardshifterIO.mapper();
        SimpleModule module = new SimpleModule("ECSModule", new Version(0, 1, 0, "alpha", "com.cardshifter", "cardshifter"));
        module.addSerializer(Entity.class, new EntitySerializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

}
