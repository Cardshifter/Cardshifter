package com.cardshifter.server.main;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.model.Server;

public class FakeAIClientTCG extends ClientIO {

	private static final Logger logger = LogManager.getLogger(FakeAIClientTCG.class);
	
	public FakeAIClientTCG(Server server) {
		super(server);
	}

	@Override
	protected void onSendToClient(String data) {
		logger.info(data);
	}

	@Override
	public void close() {
	}

}
