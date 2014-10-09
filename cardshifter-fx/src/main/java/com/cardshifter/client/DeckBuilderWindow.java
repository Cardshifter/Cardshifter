package com.cardshifter.client;

import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.client.views.CardHandDocumentController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import net.zomis.cardshifter.ecs.usage.DeckConfig;

public class DeckBuilderWindow {
	@FXML private FlowPane cardListBox;
	@FXML private VBox activeDeckBox;
	@FXML private VBox deckListBox;
	@FXML private AnchorPane previousPage;
	@FXML private AnchorPane nextPage;
	@FXML private AnchorPane exitButton;
	
	private final int cardsPerPage = 12;
	private int currentPage = 0;
	
	private Map<Integer, CardInfoMessage> cardList = new HashMap<>();
	private List<List<CardInfoMessage>> pageList = new ArrayList<>();
	
	public void acceptDeckConfig(DeckConfig deckConfig) {
		this.cardList = deckConfig.getCardData();
	}
	
	public void configureWindow() {
		this.previousPage.setOnMouseClicked(this::goToPreviousPage);
		this.nextPage.setOnMouseClicked(this::goToNextPage);
		
		this.pageList = this.listSplitter(new ArrayList<>(this.cardList.values()), this.cardsPerPage);
		
		this.displayCurrentPage();
	}
	
	private void displayCurrentPage() {
		this.cardListBox.getChildren().clear();
		for (CardInfoMessage message : this.pageList.get(this.currentPage)) {
			CardHandDocumentController card = new CardHandDocumentController(message, null);
			this.cardListBox.getChildren().add(card.getRootPane());
		}
	}
	
	private void goToPreviousPage(MouseEvent event) {
		if (this.currentPage > 0) {
			this.currentPage--;
			this.displayCurrentPage();
		}
	}
	private void goToNextPage(MouseEvent event) {
		if (this.currentPage < this.pageList.size()) {
			this.currentPage++;
			this.displayCurrentPage();
		}
	}
	
	private <T> List<List<T>> listSplitter(List<T> originalList, int splitCount) {
		List<List<T>> listOfLists= new ArrayList<>();
		listOfLists.add(new ArrayList<>());
	
		int originalListSize = originalList.size();		
		int index = 0;
		int pageNumber = 0;
		int numItemsAdded = 0;
		
		while (index < originalListSize) {
			if (numItemsAdded > splitCount - 1) {
				numItemsAdded = 0;
				pageNumber ++;
				listOfLists.add(new ArrayList<>());
			}
			List activeList = listOfLists.get(pageNumber);
			activeList.add(originalList.get(index));
			numItemsAdded++;
			index++;
		}
		
		return listOfLists;
	}
	
}
