package com.cardshifter.server.utils.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.resources.ECSResourceMap;

import com.beust.jcommander.JCommander;
import com.cardshifter.server.model.Server;
import com.cardshifter.server.model.ServerGame;
import com.cardshifter.server.model.TCGGame;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
		
		Predicate<Entity> hasResources = e -> e.hasComponent(ECSResourceMap.class);
		List<Entity> entities = g.getGameModel().findEntities(hasResources.and(e -> e.getId() < 20));
		System.out.println("Found " + entities.size() + " interesting entities to save");
		list.addAll(entities);
	}

	private static ObjectMapper createMapper() {
		XmlMapper xmlMapper = new XmlMapper();
	    SimpleModule module = new SimpleModule("ECSModule", new Version(0, 1, 0, "alpha", "com.cardshifter", "cardshifter"));
//	    // functionality includes add mix-in annotations???
	    module.addSerializer(Entity.class, new EntitySerializer());

		xmlMapper.registerModule(module);
		xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
//		xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
		return xmlMapper;
	}

}
