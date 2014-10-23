package com.cardshifter.server.utils.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import com.beust.jcommander.JCommander;
import com.cardshifter.core.game.ServerGame;
import com.cardshifter.core.game.TCGGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class DataExporter {

	public void export(Server server, String[] fullCommand) {
		ExportParameters options = new ExportParameters();
		JCommander params = new JCommander(options);
		params.parse(fullCommand);
		
		ServerGame game = server.getGames().get(options.getGameid());
		if (game == null) {
			System.out.println("Invalid game");
			return;
		}
		
		ObjectMapper mapper = createMapper();
		
		List<Entity> list = new ArrayList<>();
		gatherInterestingEntities(list, game);
		
		try {
			mapper.writeValue(new File("game-" + game.getId() + ".xml"), list);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		XmlMapper xmlMapper = new XmlMapper();
		CardshifterIO.configureMapper(xmlMapper);
	    SimpleModule module = new SimpleModule("ECSModule", new Version(0, 1, 0, "alpha", "com.cardshifter", "cardshifter"));
	    module.addSerializer(Entity.class, new EntitySerializer());

		xmlMapper.registerModule(module);
		xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
//		xmlMapper.enable(SerializationFeature.INDENT_OUTPUT); // this seem to break
		return xmlMapper;
	}

}
