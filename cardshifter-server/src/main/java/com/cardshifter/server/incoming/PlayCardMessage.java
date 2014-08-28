package com.cardshifter.server.incoming;

import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.model.Server;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayCardMessage extends CardMessage {
	
	@JsonProperty
	private String cardId;
	
	public PlayCardMessage(@JacksonInject Server server) {
		super(server);
	}

	@Override
	public void perform(ClientIO client) {
		System.out.println(cardId);
		System.out.println(getServer());
		client.sendToClient("OK");
	}
	
	public String getCardId() {
		return cardId;
	}
	
}
