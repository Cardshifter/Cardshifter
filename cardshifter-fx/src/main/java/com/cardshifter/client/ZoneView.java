package com.cardshifter.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.cardshifter.api.outgoing.UpdateMessage;
import com.cardshifter.client.views.CardView;

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
		this.rootPane.getChildren().remove(paneToRemove.getRootPane());
	}
	
	public void removeRawPane(int paneId) {
		Pane paneToRemove = this.rawPanes.remove(paneId);
		System.out.println("Remove pane " + paneId + " = " + paneToRemove);
		this.rootPane.getChildren().remove(paneToRemove);
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

}
