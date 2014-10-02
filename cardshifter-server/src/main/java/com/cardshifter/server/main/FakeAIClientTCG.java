package com.cardshifter.server.main;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.ai.CardshifterAI;
import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.model.Server;

public class FakeAIClientTCG extends ClientIO {

	private static final Logger logger = LogManager.getLogger(FakeAIClientTCG.class);
	private final CardshifterAI ai;
	
	public FakeAIClientTCG(Server server, CardshifterAI ai) {
		super(server);
		this.ai = ai;
	}

	@Override
	protected void onSendToClient(String data) {
		logger.info(data);
	}

	@Override
	public void close() {
	}

	public CardshifterAI getAI() {
		return ai;
	}

}
