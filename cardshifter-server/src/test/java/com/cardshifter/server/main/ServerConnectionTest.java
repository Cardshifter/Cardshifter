package com.cardshifter.server.main;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.api.outgoing.WaitMessage;
import com.cardshifter.api.outgoing.WelcomeMessage;
import com.cardshifter.server.model.MainServer;
import com.cardshifter.server.model.Server;
import com.cardshifter.server.model.ServerGame;

public class ServerConnectionTest {

	private MainServer main;
	private Server server;

	@Before
	public void setup() {
		PropertyConfigurator.configure(getClass().getResourceAsStream("log4j.properties"));
		main = new MainServer();
		server = main.start();
	}
	
	@After
	public void shutdown() {
		server.stop();
	}
	
	@Test(timeout = 10000)
	public void testStartGame() throws InterruptedException, UnknownHostException, IOException {
		
		TestClient client1 = new TestClient();
		client1.send(new LoginMessage("Tester"));
		
		WelcomeMessage welcome = client1.await(WelcomeMessage.class);
		assertEquals(200, welcome.getStatus());
		assertEquals(server.getClients().size(), welcome.getUserId());
		
		client1.send(new StartGameRequest(2, "VANILLA"));
		client1.await(WaitMessage.class);
		NewGameMessage gameMessage = client1.await(NewGameMessage.class);
		assertEquals(1, gameMessage.getGameId());
		ServerGame game = server.getGames().get(1);
		assertTrue(game.hasPlayer(server.getClients().get(welcome.getUserId())));
	}
	
	@Test(timeout = 10000)
	public void testPlayAny() throws InterruptedException, UnknownHostException, IOException {
		
		TestClient client1 = new TestClient();
		client1.send(new LoginMessage("Tester"));
		
		WelcomeMessage welcome = client1.await(WelcomeMessage.class);
		assertEquals(200, welcome.getStatus());
		assertEquals(server.getClients().size(), welcome.getUserId());
		
		server.getIncomingHandler().perform(new StartGameRequest(-1, "VANILLA"), server.getClients().get(1));
		client1.send(new StartGameRequest(-1, "VANILLA"));
		NewGameMessage gameMessage = client1.await(NewGameMessage.class);
		assertEquals(1, gameMessage.getGameId());
		ServerGame game = server.getGames().get(1);
		assertTrue(game.hasPlayer(server.getClients().get(welcome.getUserId())));
	}
	
}
