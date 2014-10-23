package com.cardshifter.server.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.api.CardshifterConstants;
import com.cardshifter.api.ClientIO;
import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.ServerQueryMessage;
import com.cardshifter.api.incoming.ServerQueryMessage.Request;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.outgoing.AvailableModsMessage;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.api.outgoing.UserStatusMessage;
import com.cardshifter.api.outgoing.UserStatusMessage.Status;
import com.cardshifter.api.outgoing.WaitMessage;
import com.cardshifter.api.outgoing.WelcomeMessage;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.ai.AIComponent;
import com.cardshifter.modapi.ai.CardshifterAI;
import com.cardshifter.modapi.base.ECSGameState;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.server.game.TCGGame;
import com.cardshifter.server.model.MainServer;
import com.cardshifter.server.model.Server;
import com.cardshifter.server.model.ServerGame;

public class ServerConnectionTest {

	private static final String TEST_MOD = CardshifterConstants.VANILLA;
	
	private MainServer main;
	private Server server;
	private TestClient client1;
	private int userId;

	@Before
	public void setup() throws UnknownHostException, IOException, InterruptedException {
		PropertyConfigurator.configure(getClass().getResourceAsStream("log4j.properties"));
		main = new MainServer();
		server = main.start();
		assertTrue("Server did not start correctly. Perhaps it is already running?", server.getClients().size() > 0);
		
		client1 = new TestClient();
		client1.send(new LoginMessage("Tester"));
		
		WelcomeMessage welcome = client1.await(WelcomeMessage.class);
		assertEquals(200, welcome.getStatus());
		System.out.println(server.getClients());
		assertEquals(server.getClients().size() + 1, welcome.getUserId());
		userId = welcome.getUserId();
		client1.await(ChatMessage.class);
		client1.await(AvailableModsMessage.class);
		Thread.sleep(500);
	}
	
	@After
	public void shutdown() throws InterruptedException {
		try {
			client1.disconnect();
		} catch (IOException e) {
		}
		server.stop();
	}
	
	@Test(timeout = 10000)
//	@Ignore
	public void testUserOnlineOffline() throws InterruptedException, UnknownHostException, IOException {
		
		TestClient client2 = new TestClient();
		client2.send(new LoginMessage("Test2"));
		client2.await(WelcomeMessage.class);
		client2.await(ChatMessage.class);
		
		UserStatusMessage statusMessage = client1.await(UserStatusMessage.class);
		ChatMessage chat = client1.await(ChatMessage.class);
		String message = chat.getMessage();
		assertTrue("Unexpected message: " + message, message.contains("Test2") && message.contains("joined"));
		int client2id = statusMessage.getUserId();
		assertEquals(Status.ONLINE, statusMessage.getStatus());
		assertEquals(server.getClients().size() + 1, client2id);
		assertEquals("Test2", statusMessage.getName());
		
		client2.send(new ServerQueryMessage(Request.USERS));
		client2.await(AvailableModsMessage.class);
		List<UserStatusMessage> users = client2.awaitMany(6, UserStatusMessage.class);
		System.out.println("Online users: " + users);
		// There is no determined order in which the UserStatusMessages are received, so it is harder to make any assertions.
		assertTrue(users.stream().filter(mess -> mess.getName().equals("Tester")).findAny().isPresent());
		assertTrue(users.stream().filter(mess -> mess.getName().equals("Test2")).findAny().isPresent());
		assertTrue(users.stream().filter(mess -> mess.getName().equals("AI Fighter")).findAny().isPresent());
		assertTrue(users.stream().filter(mess -> mess.getName().equals("AI Loser")).findAny().isPresent());
		assertTrue(users.stream().filter(mess -> mess.getName().equals("AI Medium")).findAny().isPresent());
		assertTrue(users.stream().filter(mess -> mess.getName().equals("AI Idiot")).findAny().isPresent());
		
		client2.disconnect();
		
		System.out.println(chat);
		statusMessage = client1.await(UserStatusMessage.class);
		assertEquals(Status.OFFLINE, statusMessage.getStatus());
		assertEquals(client2id, statusMessage.getUserId());
		assertEquals("Test2", statusMessage.getName());
	}
	
	@Test(timeout = 10000)
	public void testStartGame() throws InterruptedException, UnknownHostException, IOException {
		
		client1.send(new StartGameRequest(2, TEST_MOD));
		client1.await(WaitMessage.class);
		NewGameMessage gameMessage = client1.await(NewGameMessage.class);
		assertEquals(1, gameMessage.getGameId());
		Thread.sleep(2000);
		TCGGame game = (TCGGame) server.getGames().get(1);
		assertEquals(2, game.getGameModel().getEntitiesWithComponent(PlayerComponent.class).size());
		assertTrue(game.hasPlayer(server.getClients().get(userId)));
		assertTrue(game.hasPlayer(server.getClients().get(2)));
		game.incomingPlayerConfig(new PlayerConfigMessage(game.getId(), new HashMap<>()), server.getClients().get(2));
		game.incomingPlayerConfig(new PlayerConfigMessage(game.getId(), new HashMap<>()), server.getClients().get(userId));
		Thread.sleep(1000);
		assertEquals(ECSGameState.RUNNING, game.getState());
	}
	
	@Test(timeout = 100000)
	public void testPlayGame() throws InterruptedException, UnknownHostException, IOException {
		testPlayAny();
		Thread.sleep(1000);
		TCGGame game = (TCGGame) server.getGames().get(1);
		ClientIO io = server.getClients().get(userId);
		assertEquals(2, game.getGameModel().getEntitiesWithComponent(PlayerComponent.class).size());
		game.incomingPlayerConfig(new PlayerConfigMessage(game.getId(), new HashMap<>()), io);
		assertEquals(ECSGameState.RUNNING, game.getGameModel().getGameState());
		Entity human = game.playerFor(io);
		Entity ai = game.getGameModel().getEntitiesWithComponent(AIComponent.class).stream().findFirst().get();
		ai.getComponent(AIComponent.class).setDelay(0);
		
		CardshifterAI humanActions = new ScoringAI(AIs.medium());
		int count = 0;
		while (!game.isGameOver()) {
			System.out.println("Perform");
			ECSAction action = humanActions.getAction(human);
			if (action != null) {
				int[] targets = new int[]{ };
				System.out.println("Chosen action: " + action);
				if (!action.getTargetSets().isEmpty()) {
					targets = action.getTargetSets().get(0).getChosenTargets().stream().mapToInt(e -> e.getId()).toArray();
				}
				UseAbilityMessage message = new UseAbilityMessage(game.getId(), action.getOwner().getId(), action.getName(), targets);
				System.out.println("Sending message: " + message);
				client1.send(message);
			}
			Thread.sleep(1000);
			if (count++ > 5) {
				// no need to test the entire game
				break;
			}
		}
	}
	
	@Test(timeout = 10000)
	public void testPlayAny() throws InterruptedException, UnknownHostException, IOException {
		
		Predicate<ClientIO> opponentFilter = client -> client.getName().equals("AI Loser");
		server.getIncomingHandler().perform(new StartGameRequest(-1, TEST_MOD), server.getClients().values().stream().filter(opponentFilter).findAny().get());
		
		client1.send(new StartGameRequest(-1, TEST_MOD));
		NewGameMessage gameMessage = client1.await(NewGameMessage.class);
		assertEquals(1, gameMessage.getGameId());
		ServerGame game = server.getGames().get(1);
		assertTrue(game.hasPlayer(server.getClients().get(userId)));
		Thread.sleep(1000);
//		assertEquals(ECSGameState.RUNNING, game.getState());
	}
	
}
