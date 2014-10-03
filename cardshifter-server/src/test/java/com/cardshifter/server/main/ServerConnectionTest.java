package com.cardshifter.server.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.function.Predicate;

import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.ai.AIComponent;
import net.zomis.cardshifter.ecs.base.ECSGameState;
import net.zomis.cardshifter.ecs.base.Entity;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.CardshifterAI;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.api.outgoing.WaitMessage;
import com.cardshifter.api.outgoing.WelcomeMessage;
import com.cardshifter.server.model.ClientIO;
import com.cardshifter.server.model.GameState;
import com.cardshifter.server.model.MainServer;
import com.cardshifter.server.model.Server;
import com.cardshifter.server.model.ServerGame;
import com.cardshifter.server.model.TCGGame;

public class ServerConnectionTest {

	private MainServer main;
	private Server server;
	private TestClient client1;

	@Before
	public void setup() throws UnknownHostException, IOException {
		PropertyConfigurator.configure(getClass().getResourceAsStream("log4j.properties"));
		main = new MainServer();
		server = main.start();
		
		client1 = new TestClient();
		client1.send(new LoginMessage("Tester"));
	}
	
	@After
	public void shutdown() {
		server.stop();
	}
	
	@Test(timeout = 10000)
	public void testStartGame() throws InterruptedException, UnknownHostException, IOException {
		
		WelcomeMessage welcome = client1.await(WelcomeMessage.class);
		assertEquals(200, welcome.getStatus());
		assertEquals(server.getClients().size(), welcome.getUserId());
		
		client1.send(new StartGameRequest(2, "VANILLA"));
		client1.await(WaitMessage.class);
		NewGameMessage gameMessage = client1.await(NewGameMessage.class);
		assertEquals(1, gameMessage.getGameId());
		ServerGame game = server.getGames().get(1);
		assertTrue(game.hasPlayer(server.getClients().get(welcome.getUserId())));
		Thread.sleep(1000);
		assertEquals(GameState.RUNNING, game.getState());
	}
	
	@Test(timeout = 100000)
	public void testPlayGame() throws InterruptedException, UnknownHostException, IOException {
		testPlayAny();
		Thread.sleep(1000);
		TCGGame game = (TCGGame) server.getGames().get(1);
		assertEquals(ECSGameState.RUNNING, game.getGameModel().getGameState());
		ClientIO io = server.getClients().get(server.getClients().size());
		Entity human = game.playerFor(io);
		Entity ai = game.getGameModel().getEntitiesWithComponent(AIComponent.class).stream().findFirst().get();
		ai.getComponent(AIComponent.class).setDelay(10);
		
		CardshifterAI humanActions = new ScoringAI(AIs.medium());
		while (!game.isGameOver()) {
			System.out.println("Perform");
			ECSAction action = humanActions.getAction(human);
			if (action != null) {
				int target = 0;
				if (!action.getTargetSets().isEmpty()) {
					target = action.getTargetSets().get(0).getChosenTargets().get(0).getId();
				}
				UseAbilityMessage message = new UseAbilityMessage(game.getId(), action.getOwner().getId(), action.getName(), target);
				System.out.println("Sending message: " + message);
				client1.send(message);
			}
			Thread.sleep(200);
		}
	}
	
	@Test(timeout = 10000)
	public void testPlayAny() throws InterruptedException, UnknownHostException, IOException {
		
		WelcomeMessage welcome = client1.await(WelcomeMessage.class);
		assertEquals(200, welcome.getStatus());
		assertEquals(server.getClients().size(), welcome.getUserId());
		
		Predicate<ClientIO> opponentFilter = client -> client.getName().equals("AI loser");
		server.getIncomingHandler().perform(new StartGameRequest(-1, "VANILLA"), server.getClients().values().stream().filter(opponentFilter).findAny().get());
		
		client1.send(new StartGameRequest(-1, "VANILLA"));
		NewGameMessage gameMessage = client1.await(NewGameMessage.class);
		assertEquals(1, gameMessage.getGameId());
		ServerGame game = server.getGames().get(1);
		assertTrue(game.hasPlayer(server.getClients().get(welcome.getUserId())));
		Thread.sleep(1000);
		assertEquals(GameState.RUNNING, game.getState());
	}
	
}
