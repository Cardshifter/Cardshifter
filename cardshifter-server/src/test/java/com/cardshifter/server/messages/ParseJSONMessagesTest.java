package com.cardshifter.server.messages;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.cardshifter.server.incoming.UseAbilityMessage;
import com.cardshifter.server.main.ServerMain;
import com.cardshifter.server.model.Server;

public class ParseJSONMessagesTest {

	@Test
	public void parse() throws IOException {
		PropertyConfigurator.configure(ServerMain.class.getResourceAsStream("log4j.properties"));
		Server server = new Server();
		UseAbilityMessage message = server.getIncomingHandler().parse("{ \"command\": \"use\", \"gameId\": 1, \"action\": \"Play\", \"id\": \"2\", \"target\": 0 }");
		assertEquals(2, message.getId());
		assertEquals(0, message.getTarget());
		assertEquals("use", message.getCommand());
		assertEquals(1, message.getGameId());
		assertEquals("Play", message.getAction());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void parseUnknownCommand() throws IOException {
		PropertyConfigurator.configure(ServerMain.class.getResourceAsStream("log4j.properties"));
		Server server = new Server();
		server.getIncomingHandler().parse("{ \"command\": \"jibberish\", \"requestId\": 1, \"cardId\": \"12\" }");
	}
	
	@Test(expected = IOException.class)
	public void parseUnknownData() throws IOException {
		PropertyConfigurator.configure(ServerMain.class.getResourceAsStream("log4j.properties"));
		Server server = new Server();
		server.getIncomingHandler().parse("{ \"command\": \"use\", \"jibberishId\": 1, \"id\": 12 }");
		
	}
}
