package com.cardshifter.client;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.layout.Pane;

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
	
}
