package com.cardshifter.client;

import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.client.views.CardHandDocumentController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	
	private static final int CARDS_PER_PAGE = 12;
	private int currentPage = 0;
	
	private Map<Integer, CardInfoMessage> cardList = new HashMap<>();
	private List<List<CardInfoMessage>> pageList = new ArrayList<>();
	
	public void acceptDeckConfig(DeckConfig deckConfig) {
		this.cardList = deckConfig.getCardData();
	}
	
	public void configureWindow() {
		this.previousPage.setOnMouseClicked(this::goToPreviousPage);
		this.nextPage.setOnMouseClicked(this::goToNextPage);
		
		this.pageList = listSplitter(new ArrayList<>(this.cardList.values()), CARDS_PER_PAGE);
		
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
		if (this.currentPage < this.pageList.size() - 1) {
			this.currentPage++;
			this.displayCurrentPage();
		}
	}
	
	private static <T> List<List<T>> listSplitter(List<T> originalList, int resultsPerList) {
		if (resultsPerList <= 0) {
			throw new IllegalArgumentException("resultsPerList must be positive");
		}
		List<List<T>> listOfLists = new ArrayList<>();
		List<T> latestList = new ArrayList<>();
		Iterator<T> iterator = originalList.iterator();

	    while (iterator.hasNext()) {
		    T next = iterator.next();
			if (latestList.size() >= resultsPerList) {
				listOfLists.add(latestList);
				latestList = new ArrayList<>();
			} 
			latestList.add(next);
		}

		if (!latestList.isEmpty()) {
			listOfLists.add(latestList);
		}

		return listOfLists;
	}

}
