package com.cardshifter.server.main;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.outgoing.WelcomeMessage;
import com.cardshifter.server.model.MainServer;
import com.cardshifter.server.model.Server;

public class ServerConnectionTest {

	@Test
	public void testStartGame() throws InterruptedException, UnknownHostException, IOException {
		PropertyConfigurator.configure(getClass().getResourceAsStream("log4j.properties"));
		
		MainServer main = new MainServer();
		Server server = main.start();
		
		TestClient client1 = new TestClient();
		client1.send(new LoginMessage("Tester"));
		
		WelcomeMessage welcome = client1.await(WelcomeMessage.class);
		assertEquals(200, welcome.getStatus());
		
		
		
		
		
		server.stop();
	}
	
}
