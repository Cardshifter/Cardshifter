package com.cardshifter.server.messages;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.cardshifter.server.clients.FakeClient;
import com.cardshifter.server.main.ServerMain;
import com.cardshifter.server.model.Server;

public class PlayCardMessageTest {

	@Test
	public void parse() {
		PropertyConfigurator.configure(ServerMain.class.getResourceAsStream("log4j.properties"));
		Server server = new Server();
		
		FakeClient client = new FakeClient(server, System.out::println);
		client.sentToServer("{ \"command\": \"playCard\", \"requestId\": 1, \"cardId\": \"12\" }");
		
		
	}
	
}
