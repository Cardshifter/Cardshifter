package net.zomis.cardshifter.ecs.usage;

import net.zomis.cardshifter.ecs.config.DeckConfig;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CardshifterIO {

	public static void configureMapper(ObjectMapper mapper) {
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		mapper.registerSubtypes(DeckConfig.class);
	}
	
	public static ObjectMapper mapper() {
		ObjectMapper mapper = new ObjectMapper();
		configureMapper(mapper);
		return mapper;
	}
	
}
