package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.api.config.DeckConfig;

import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.WelcomeMessage;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class CardshifterIO {

	public static void configureMapper(ObjectMapper mapper) {
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		mapper.registerSubtypes(DeckConfig.class);
		
		SimpleModule module = new SimpleModule("", new Version(0, 5, 0, "", "com.cardshifter", "cardshifter"));
		module.setMixInAnnotation(DeckConfig.class, MixinDeckConfig.class);
		module.setMixInAnnotation(Message.class, MixinMessage.class);
        module.setMixInAnnotation(CardInfoMessage.class, MixinCardInfoMessage.class);
        module.setMixInAnnotation(PlayerConfigMessage.class, MixinPlayerConfigMessage.class);
        module.setMixInAnnotation(WelcomeMessage.class, MixinWelcomeMessage.class);
		mapper.registerModule(module);
	}
	
	public static ObjectMapper mapper() {
		ObjectMapper mapper = new ObjectMapper();
		configureMapper(mapper);
		return mapper;
	}
	
}
