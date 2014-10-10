package com.cardshifter.client;

import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.client.views.CardHandDocumentController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import net.zomis.cardshifter.ecs.usage.DeckConfig;

public class DeckBuilderWindow {
	@FXML private FlowPane cardListBox;
	@FXML private VBox activeDeckBox;
	@FXML private VBox deckListBox;
	//@FXML private AnchorPane activeDeckAnchorPane;
	@FXML private AnchorPane previousPage;
	@FXML private AnchorPane nextPage;
	@FXML private AnchorPane exitButton;
	
	private PlayerConfigMessage currentPlayerConfig;
	private GameClientLobby lobby;
	private static final int CARDS_PER_PAGE = 12;
	private int currentPage = 0;
	
	private Map<Integer, CardInfoMessage> cardList = new HashMap<>();
	private List<List<CardInfoMessage>> pageList = new ArrayList<>();
	
	public void acceptPlayerConfig(PlayerConfigMessage message, GameClientLobby lobby) {
		this.currentPlayerConfig = message;
		this.lobby = lobby;
		
		Map<String, Object> configs = message.getConfigs();
		
		for (Map.Entry<String, Object> entry : configs.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof DeckConfig) {
				DeckConfig deckConfig = (DeckConfig) value;
				this.cardList = deckConfig.getCardData();
			}
		}
	}
	
	public void configureWindow() {
		this.previousPage.setOnMouseClicked(this::goToPreviousPage);
		this.nextPage.setOnMouseClicked(this::goToNextPage);
		
		this.activeDeckBox.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				System.out.println("helpme");
			}
		});
		
		this.activeDeckBox.setOnDragDropped(e -> this.receiveDrag(e, true));
		//this.activeDeckBox.setOnDragEntered(e -> {this.receiveDrag(e);});
		this.activeDeckBox.setOnDragOver(e -> this.receiveDrag(e, false));
		//this.activeDeckAnchorPane.setOnDragDropped(e -> {this.receiveDrag(e);});
		
		this.pageList = listSplitter(new ArrayList<>(this.cardList.values()), CARDS_PER_PAGE);
		
		this.displayCurrentPage();
	}
	
	private void displayCurrentPage() {
		this.cardListBox.getChildren().clear();
		for (CardInfoMessage message : this.pageList.get(this.currentPage)) {
			CardHandDocumentController card = new CardHandDocumentController(message, null);
			Pane cardPane = card.getRootPane();
			cardPane.setOnDragDetected(e -> this.reportDrag(e, cardPane, card));
			
			
			cardPane.setOnDragDone(event -> System.out.println("dropped it"));
			
			
			
			this.cardListBox.getChildren().add(cardPane);
		}
	}
	
	private void reportDrag(MouseEvent event, Pane pane, CardHandDocumentController card) {
		Dragboard db = pane.startDragAndDrop(TransferMode.MOVE);
		ClipboardContent content = new ClipboardContent();
		content.put(DataFormat.RTF, pane);
		content.putString(card.toString());
		db.setContent(content);
		
		System.out.println("drag detected");
		System.out.println(card.toString());
		
		event.consume();
	}
	
	private void receiveDrag(DragEvent event, boolean dropped) {
		System.out.println("drag dropped");
//		this.activeDeckBox.getChildren().add(new Label(event.getDragboard().getString()));
//		event.setDropCompleted(true);
		
		event.acceptTransferModes(TransferMode.MOVE);
		if (dropped) {
			this.activeDeckBox.getChildren().add(new Label(event.getDragboard().getString()));
		}
		event.consume();
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
	
	private CardInfoMessage getCardForListIndex(int listIndex) {
		//int pageNumber = this.currentPage;
		
		return this.cardList.get(listIndex); //placeholder
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
