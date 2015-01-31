package com.cardshifter.client.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.cardshifter.api.outgoing.UpdateMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;

public class ZoneView<T extends CardView> {
	
	private final int zoneId;
	private final Pane rootPane;
	private final Map<Integer, T> zoneMap = new HashMap<>();
	private final Map<Integer, Pane> rawPanes = new HashMap<>();
	
	public ZoneView(int zoneId, Pane pane) {
		this.zoneId = zoneId;
		this.rootPane = pane;
	}
	
	public void addPane(int paneId, T pane) {
		this.zoneMap.put(paneId, pane);
		this.rootPane.getChildren().add(pane.getRootPane());
	}
	
	public void addSimplePane(int paneId, Pane pane) {
		this.rawPanes.put(paneId, pane);
		this.rootPane.getChildren().add(pane);
		System.out.println("Add raw pane " + paneId + " = " + pane);
	}
	
	public T getCard(int id) {
		return zoneMap.get(id);
	}
	
	public T getPane(int paneId) {
		return this.zoneMap.get(paneId);
	}
	
	public void removePane(int paneId) {
		T paneToRemove = this.zoneMap.remove(paneId);
		if (paneToRemove != null) {
			this.rootPane.getChildren().remove(paneToRemove.getRootPane());
		}
		
		Pane rawPaneToRemove = this.rawPanes.remove(paneId);
		if (rawPaneToRemove != null) {
			this.rootPane.getChildren().remove(rawPaneToRemove);
		}
	}
	
	public int getId() {
		return this.zoneId;
	}
	
	public int getSize() {
		return this.zoneMap.size() + this.rawPanes.size();
	}
	
	public Pane getRootPane() {
		return this.rootPane;
	}
	
	public Set<Integer> getAllIds() {
		return this.zoneMap.keySet();
	}
	
	public void updateCard(int cardId, UpdateMessage message) {
		T card = getCard(cardId);
		card.updateFields(message);
	}
	
	//This causes a Null Pointer Exception, don't know why
	public void highlightCard(int cardId) {
		Pane pane = this.getPane(cardId).getRootPane();
		List<Node> children = pane.getChildren();
		for (Node node : children) {
			if (node.getId().equals("backgroundRectangle")) {
				Rectangle rectangle = (Rectangle)node;
				rectangle.setFill(Color.YELLOW);
			}
		}
	}

	public void setCardActive(int id, UsableActionMessage message) {
		T card = getCard(id);
		card.setCardActive(message);
	}

	public void removeActiveAllCards() {
		zoneMap.values().forEach(card -> card.removeCardActive());
	}
	
	public void removeScrappableAllCards() {
		zoneMap.values().forEach(card -> card.removeCardScrappable());
	}

	public void setCardTargetable(int target) {
		T card = getCard(target);
		card.setCardTargetable();
	}
	
	public void setCardScrappable(int target, UsableActionMessage message) {
		T card = getCard(target);
		card.setCardScrappable(message);
	}

	public boolean contains(int id) {
		return zoneMap.containsKey(id) || rawPanes.containsKey(id);
	}
	
}
