package com.cardshifter.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ZoneView {
	
	private final int zoneId;
	private final Pane rootPane;
	private final Map<Integer, Pane> zoneMap = new HashMap<>();
	
	public ZoneView(int zoneId, Pane pane) {
		this.zoneId = zoneId;
		this.rootPane = pane;
	}
	
	public void addPane(int paneId, Pane pane) {
		this.zoneMap.put(paneId, pane);
		this.rootPane.getChildren().add(pane);
	}
	
	public Pane getPane(int paneId) {
		return this.zoneMap.get(paneId);
	}
	
	public void removePane(int paneId) {
		Pane paneToRemove = this.zoneMap.get(paneId);
		this.rootPane.getChildren().remove(paneToRemove);
		this.zoneMap.remove(paneId);
	}
	
	public int getId() {
		return this.zoneId;
	}
	
	public int getSize() {
		return this.zoneMap.size();
	}
	
	public Pane getRootPane() {
		return this.rootPane;
	}
	
	public Set getAllIds() {
		return this.zoneMap.keySet();
	}
	
	public void highlightCard(int cardId) {
		Pane pane = this.getPane(cardId);
		List<Node> children = pane.getChildren();
		for (Node node : children) {
			if (node.getId().equals("backgroundRectangle")) {
				Rectangle rectangle = (Rectangle)node;
				rectangle.setFill(Color.YELLOW);
			}
		}
	}
	
}
