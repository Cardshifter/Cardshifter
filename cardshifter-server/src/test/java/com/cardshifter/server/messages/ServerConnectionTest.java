package com.cardshifter.server.messages;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cardshifter.server.model.MainServer;
import com.cardshifter.server.model.Server;

public class ServerConnectionTest {

	@Test
	public void testStartGame() throws InterruptedException {
		
		MainServer main = new MainServer();
		Server server = main.start();
		
		Thread.sleep(10000);
		
		server.stop();
		
		assertEquals(0, 0);
	}
	
}
