package com.cardshifter.server.utils.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.zomis.cardshifter.ecs.usage.CardshifterIO;

import com.beust.jcommander.JCommander;
import com.cardshifter.modapi.base.CreatureTypeComponent;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.server.model.Server;
import com.cardshifter.server.model.ServerGame;
import com.cardshifter.server.model.TCGGame;
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
		
		Predicate<Entity> hasResources = e -> e.hasComponent(ECSResourceMap.class);
		Entity p1 = g.getGameModel().findEntities(e -> e.hasComponent(PlayerComponent.class) && e.getComponent(PlayerComponent.class).getIndex() == 0).iterator().next();
		Predicate<Entity> isPlayer1 = e -> e.hasComponent(CardComponent.class) && e.getComponent(CardComponent.class).getOwner() == p1;
		List<Entity> entities = g.getGameModel().findEntities(hasResources.and(isPlayer1)); // .and(e -> e.getId() < 50));
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
