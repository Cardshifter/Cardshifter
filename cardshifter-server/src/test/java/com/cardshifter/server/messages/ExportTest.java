package com.cardshifter.server.messages;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.cardshifter.api.CardshifterConstants;
import com.cardshifter.server.main.ServerMain;
import com.cardshifter.server.model.MainServer;
import com.cardshifter.server.model.Server;
import com.cardshifter.server.utils.export.DataExporter;

public class ExportTest {

	@Test
	public void test() {
		PropertyConfigurator.configure(ServerMain.class.getResourceAsStream("log4j.properties"));
		
		Server server = new MainServer().start();
		
		server.createGame(CardshifterConstants.VANILLA);
		DataExporter exporter = new DataExporter();
		exporter.export(server, new String[]{ "--gameid", "1" });
		
		server.stop();
	}
	
}
