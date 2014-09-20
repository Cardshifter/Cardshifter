package com.cardshifter.client;

import com.cardshifter.server.outgoing.UseableActionMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javafx.scene.layout.Pane;

public class BattlefieldZoneView extends ZoneView {
	
	private final Map<Integer, CardBattlefieldDocumentController> cardMap = new HashMap<>();
	
	public BattlefieldZoneView(int cardId, Pane pane) {
		super(cardId, pane);
	}
	
	public void addCardHandController(int cardId, CardBattlefieldDocumentController controller) {
		this.cardMap.put(cardId, controller);
		super.addPane(cardId, controller.getRootPane());
	}
	
	public void removeCardHandDocumentController(int cardId) {
		super.removePane(cardId);
		this.cardMap.remove(cardId);
	}
	
	public void removeSicknessForCard(int cardId) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		card.removeSickness();
	}
	
	public CardBattlefieldDocumentController getCardHandController(int cardId) {
		return this.cardMap.get(cardId);
	}
	
	@Override
	public int getSize() {
		return this.cardMap.size();
	}
	
	@Override
	public Set getAllIds() {
		return this.cardMap.keySet();
	}
	
	public void setCardActive(int cardId, UseableActionMessage message) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		card.setCardActive(message);
		super.removePane(cardId);
		super.addPane(cardId, card.getRootPane());
	}
	
	private void removeCardActive(int cardId) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		if (card.isCardActive()) {
			card.removeCardActive();
			super.removePane(cardId);
			super.addPane(cardId, card.getRootPane());
		}
	}
	
	public void removeActiveAllCards() {
		for (Object cardId : this.getAllIds()) {
			this.removeCardActive((int)cardId);
		}
	}
	
	public void setCardTargetable(int cardId, UseableActionMessage message) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		card.setCardTargetable(message);
		super.removePane(cardId);
		super.addPane(cardId, card.getRootPane());
	}
	
}