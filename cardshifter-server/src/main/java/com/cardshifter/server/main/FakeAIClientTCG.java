package com.cardshifter.server.main;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.api.ClientIO;
import com.cardshifter.api.ClientServerInterface;
import com.cardshifter.api.messages.Message;
import com.cardshifter.modapi.ai.CardshifterAI;

public class FakeAIClientTCG extends ClientIO {

	private static final Logger logger = LogManager.getLogger(FakeAIClientTCG.class);
	private final CardshifterAI ai;
	
	public FakeAIClientTCG(ClientServerInterface server, CardshifterAI ai) {
		super(server);
		this.ai = ai;
	}
	
	@Override
	protected void onSendToClient(Message data) {
		logger.info(data);
	}

	@Override
	public void close() {
	}

	public CardshifterAI getAI() {
		return ai;
	}

	@Override
	public String getRemoteAddress() {
		return "FakeAI: " + ai;
	}

}
