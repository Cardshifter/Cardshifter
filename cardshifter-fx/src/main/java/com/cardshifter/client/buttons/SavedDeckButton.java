package com.cardshifter.client.buttons;

import com.cardshifter.client.DeckBuilderWindow;
import javafx.scene.input.MouseEvent;

public class SavedDeckButton extends GenericButton {
	
	private final DeckBuilderWindow controller;

	public SavedDeckButton (double sizeX, double sizeY, String deckName, DeckBuilderWindow controller) {
		super(sizeX, sizeY, deckName);
		this.controller = controller;
		
		super.setUpRectangle();
		super.setUpLabel();
		
		this.setOnMouseClicked(this::actionButtonClicked);
	}
	
	@Override
	public void actionButtonClicked(MouseEvent event) {
		this.controller.clearSavedDeckButtons();
		this.controller.setDeckToLoad(super.getButtonString());
		this.highlightButton();
	}
}
