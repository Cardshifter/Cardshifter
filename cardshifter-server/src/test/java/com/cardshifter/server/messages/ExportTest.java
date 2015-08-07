package com.cardshifter.server.messages;

import java.util.Arrays;

import com.cardshifter.server.commands.CommandContext;
import com.cardshifter.server.main.ServerConfiguration;
import com.cardshifter.server.model.Command;
import com.cardshifter.server.utils.export.DataExportCommand;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.cardshifter.core.game.FakeClient;
import com.cardshifter.core.game.ServerGame;
import com.cardshifter.server.main.ServerMain;
import com.cardshifter.server.model.MainServer;
import com.cardshifter.server.model.Server;

import static org.junit.Assert.assertTrue;

public class ExportTest {

	private final int MAX_SERVER_PORT_TRY = 10;

	@Test
	public void test() {
		PropertyConfigurator.configure(ServerMain.class.getResourceAsStream("log4j.properties"));

		ServerConfiguration config = ServerConfiguration.defaults();
		int basePortSocket = config.getPortSocket();
		int basePortWebsocket = config.getPortWebsocket();
		Server server = null;

		// The ports might be in use by another instance or application
		// Could use port = 0, but would need access to the ServerSocket to get the real port number
		for (int i = 0; i < MAX_SERVER_PORT_TRY; i++) {
			config.setPortSocket(basePortSocket + i * 10);
			config.setPortWebsocket(basePortWebsocket + i * 10);

			server = new MainServer(config).start();

			if (server.getClients().size() > 0) {
				break;
			}
		}

		assertTrue("Server did not start correctly after " + MAX_SERVER_PORT_TRY + " retries.",
				server != null && server.getClients().size() > 0);

		String modName = server.getGameFactories().keySet().iterator().next();
		ServerGame game = server.createGame(modName);
        FakeClient fakeClient = new FakeClient(server, e -> {});
		game.start(Arrays.asList(fakeClient, new FakeClient(server, e -> {})));
		DataExportCommand exporter = new DataExportCommand();
        DataExportCommand.DataExportParameters params = new DataExportCommand.DataExportParameters();
        params.gameId = 1;
		exporter.handle(new CommandContext(server, new Command(fakeClient, "-game 1"), fakeClient), params);
		
		server.stop();
	}
	
}
