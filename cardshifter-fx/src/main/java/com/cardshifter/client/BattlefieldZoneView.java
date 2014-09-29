package com.cardshifter.client;

import com.cardshifter.api.outgoing.UpdateMessage;
import com.cardshifter.api.outgoing.UseableActionMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javafx.scene.layout.Pane;

public class BattlefieldZoneView extends ZoneView {
	
	private final Map<Integer, CardBattlefieldDocumentController> cardMap = new HashMap<>();
	
	public BattlefieldZoneView(int cardId, Pane pane) {
		super(cardId, pane);
	}
	
	public void addCardController(int cardId, CardBattlefieldDocumentController controller) {
		super.addPane(cardId, controller.getRootPane());
		this.cardMap.put(cardId, controller);
	}
	
	public void removeCardController(int cardId) {
		super.removePane(cardId);
		this.cardMap.remove(cardId);
	}
	
	public void removeSicknessForCard(int cardId) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		card.removeSickness();
	}
	
	public void setCardCanAttack(int cardId, UseableActionMessage message) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		card.setCardAttackActive(message);
	}
	
	public void setCardActive(int cardId, UseableActionMessage message) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		card.setCardActive(message);
	}
	
	public void removeActiveAllCards() {
		for (Object cardId : this.getAllIds()) {
			this.removeCardActive((int)cardId);
		}
	}
	
	private void removeCardActive(int cardId) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		if (card.isCardActive()) {
			card.removeCardActive();
		}
	}
	
	public void removeTargetableAllCards() {
		for (Object cardId : this.getAllIds()) {
			this.removeCardTargetable((int)cardId);
		}
	}
	
	public void removeCardTargetable(int cardId) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		card.removeCardActive();
	}

	public void setCardTargetable(int cardId, UseableActionMessage message) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		card.setCardTargetable(message);
	}
	
	public void updateCard(int cardId, UpdateMessage message) {
		CardBattlefieldDocumentController card = this.cardMap.get(cardId);
		card.updateFields(message);
	}
	
	@Override
	public int getSize() {
		return this.cardMap.size();
	}
	
	@Override
	public Set getAllIds() {
		return this.cardMap.keySet();
	}
	
}