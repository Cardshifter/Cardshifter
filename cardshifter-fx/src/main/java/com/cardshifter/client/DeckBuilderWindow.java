package com.cardshifter.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import net.zomis.cardshifter.ecs.usage.CardshifterIO;
import net.zomis.cardshifter.ecs.usage.DeckConfig;

import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.client.buttons.GenericButton;
import com.cardshifter.client.buttons.SavedDeckButton;
import com.cardshifter.client.views.CardHandDocumentController;
import com.cardshifter.client.views.DeckCardController;

public class DeckBuilderWindow {
	
	@FXML private AnchorPane rootPane;
	@FXML private FlowPane cardListBox;
	@FXML private VBox activeDeckBox;
	@FXML private VBox deckListBox;
	@FXML private AnchorPane previousPage;
	@FXML private AnchorPane nextPage;
	@FXML private AnchorPane saveDeckButton;
	@FXML private AnchorPane loadDeckButton;
	@FXML private AnchorPane clearDeckButton;
	@FXML private TextField deckNameBox;
	@FXML private AnchorPane exitButton;
	@FXML private AnchorPane deleteButton;
	@FXML private Label cardCountLabel;
	
	private static final int CARDS_PER_PAGE = 12;
	private int currentPage = 0;
	private Map<Integer, CardInfoMessage> cardList = new HashMap<>();
	private List<List<CardInfoMessage>> pageList = new ArrayList<>();
	private DeckConfig activeDeckConfig;
	private CardInfoMessage cardBeingDragged;
	private Consumer<DeckConfig> configCallback;
	private String deckToLoad;
	
	public void acceptDeckConfig(DeckConfig deckConfig, Consumer<DeckConfig> configCallback) {
		this.configCallback = configCallback;
		this.activeDeckConfig = deckConfig;
		this.cardList = deckConfig.getCardData();
	}
	
	public void configureWindow() {
		this.previousPage.setOnMouseClicked(this::goToPreviousPage);
		this.nextPage.setOnMouseClicked(this::goToNextPage);
		this.exitButton.setOnMouseClicked(this::startGame);
		this.saveDeckButton.setOnMouseClicked(this::saveDeck);
		this.loadDeckButton.setOnMouseClicked(this::loadDeck);
		this.deleteButton.setOnMouseClicked(this::deleteDeck);
		this.clearDeckButton.setOnMouseClicked(this::clearDeck);
		this.activeDeckBox.setOnDragDropped(e -> this.completeDragToActiveDeck(e, true));
		this.activeDeckBox.setOnDragOver(e -> this.completeDragToActiveDeck(e, false));
		
		List<CardInfoMessage> sortedCardList = new ArrayList<>(this.cardList.values());
		Collections.sort(sortedCardList, Comparator.comparingInt(msg -> msg.getId()));
		this.pageList = listSplitter(sortedCardList, CARDS_PER_PAGE);
		
		this.displayCurrentPage();
		this.displaySavedDecks();
	}
	
	public void disableGameStart() {
		this.exitButton.setVisible(false);
	}
	
	private void startGame(MouseEvent event) {
		if (this.activeDeckConfig.getTotal() == this.activeDeckConfig.getMaxSize()) {
			this.configCallback.accept(this.activeDeckConfig);
			this.closeWindow();
		}
	}
	
	private void displayCurrentPage() {
		this.cardListBox.getChildren().clear();
		for (CardInfoMessage message : this.pageList.get(this.currentPage)) {
			CardHandDocumentController card = new CardHandDocumentController(message, null);
			Pane cardPane = card.getRootPane();			
			
			Rectangle numberOfCardsBox = new Rectangle(cardPane.getPrefWidth()/3, cardPane.getPrefHeight()/10);
			numberOfCardsBox.setFill(Color.BLUE);
			numberOfCardsBox.setStroke(Color.BLACK);
			int numChosenCards = 0;
			if (this.activeDeckConfig.getChosen().get(message.getId()) != null) {
				numChosenCards = this.activeDeckConfig.getChosen().get(message.getId());
			}
			Label numberOfCardsLabel = new Label(String.format("%d / %d", numChosenCards, this.activeDeckConfig.getMaxPerCard()));
			numberOfCardsLabel.setTextFill(Color.WHITE);
			numberOfCardsBox.relocate(cardPane.getPrefWidth()/2.6, cardPane.getPrefHeight() - cardPane.getPrefHeight()/18);
			numberOfCardsLabel.relocate(cardPane.getPrefWidth()/2.3, cardPane.getPrefHeight() - cardPane.getPrefHeight()/18);
			cardPane.getChildren().add(numberOfCardsBox);
			cardPane.getChildren().add(numberOfCardsLabel);
			
			cardPane.setOnMouseClicked(e -> {this.addCardToActiveDeck(e, message);});
			cardPane.setOnDragDetected(e -> this.startDragToActiveDeck(e, cardPane, message));
			this.cardListBox.getChildren().add(cardPane);
		}
	}

	private void displaySavedDecks() {
		this.deckListBox.getChildren().clear();
		File dir = new File(".");
		if (dir.listFiles() != null) {
			for (File file : dir.listFiles()) {
				try {
					if ((CardshifterIO.mapper().readValue(file, DeckConfig.class) instanceof DeckConfig)) {
						SavedDeckButton deckButton = new SavedDeckButton(this.deckListBox.getPrefWidth(), 40, file.getName(), this);
						this.deckListBox.getChildren().add(deckButton);
					}
				} catch (Exception e) {
					//swallowing this exception because it clogs the logs
					//happens every time the file is not the proper format
					//System.out.println("File failed to load");
				}				
			}
		}
	}
	
	private void displayActiveDeck() {
		this.activeDeckBox.getChildren().clear();
		List<Integer> sortedKeys = new ArrayList<>(this.activeDeckConfig.getChosen().keySet());
		Collections.sort(sortedKeys, Comparator.comparingInt(key -> key));
		for (Integer cardId : sortedKeys) {
			if (!cardList.containsKey(cardId)) {
				activeDeckConfig.setChosen(cardId, 0);
				continue;
			}
			DeckCardController card = new DeckCardController(this.cardList.get(cardId), this.activeDeckConfig.getChosen().get(cardId));
			Pane cardPane = card.getRootPane();
			cardPane.setOnMouseClicked(e -> {this.removeCardFromDeck(e, cardId);});
			this.activeDeckBox.getChildren().add(cardPane);
		}
		this.cardCountLabel.setText(String.format("%d / %d", this.activeDeckConfig.getTotal(), this.activeDeckConfig.getMaxSize()));
	}

	private void addCardToActiveDeck(MouseEvent event, CardInfoMessage message) {
		if (this.activeDeckConfig.getTotal() < this.activeDeckConfig.getMaxSize()) {
			if(this.activeDeckConfig.getChosen().get(message.getId()) == null) {
				this.activeDeckConfig.setChosen(message.getId(), 1);
			} else {
				if (this.activeDeckConfig.getChosen().get(message.getId()) < this.activeDeckConfig.getMaxPerCard()) {
					this.activeDeckConfig.setChosen(message.getId(), this.activeDeckConfig.getChosen().get(message.getId()) + 1);
				}
			}
		}
		this.displayActiveDeck();
		this.displayCurrentPage();
	}
	
	private void removeCardFromDeck(MouseEvent event, int cardId) {
		if (this.activeDeckConfig.getChosen().get(cardId) != null) {
			this.activeDeckConfig.removeChosen(cardId);
		}
		this.displayActiveDeck();
		this.displayCurrentPage();
	}
	
	private void startDragToActiveDeck(MouseEvent event, Pane pane, CardInfoMessage message) {
		this.cardBeingDragged = message;
 		Dragboard db = pane.startDragAndDrop(TransferMode.MOVE);
 		ClipboardContent content = new ClipboardContent();
 		content.putString(message.toString());
 		db.setContent(content);
		event.consume();
 	}
	
	private void completeDragToActiveDeck(DragEvent event, boolean dropped) {
		event.acceptTransferModes(TransferMode.MOVE);
		if (dropped) {
			this.addCardToActiveDeck(null, this.cardBeingDragged);
		}
		event.consume();
 	}

	public void setDeckToLoad(String deckName) {
		this.deckToLoad = deckName;
	}

	private void clearDeck(MouseEvent event) {		
		this.activeDeckConfig.clearChosen();
		this.displayActiveDeck();
		this.displayCurrentPage();
	}
	
	private void saveDeck(MouseEvent event) {
		if(!this.deckNameBox.textProperty().get().isEmpty()) {
			try {
				File file = new File(this.deckNameBox.textProperty().get() + ".deck");
				if (file.isFile()) {
					System.out.println("Deck already exists");
				} else {
					CardshifterIO.mapper().writeValue(new File(this.deckNameBox.textProperty().get() + ".deck"), this.activeDeckConfig);
				}
			} catch (Exception e) {
				System.out.println("Deck failed to save");
			}
		}
		this.displaySavedDecks();
	}
	
	private void loadDeck(MouseEvent event) {
		if (this.deckToLoad != null) {
			try {
				this.activeDeckConfig = CardshifterIO.mapper().readValue(new File(this.deckToLoad), DeckConfig.class);
				String truncatedDeckName = this.deckToLoad.substring(0, this.deckToLoad.length()- 5);
				this.deckNameBox.textProperty().set(truncatedDeckName);
			} catch (Exception e) {
				System.out.println("Deck failed to load");
			}
		}
		this.displayActiveDeck();
		this.displayCurrentPage();
	}
	
	private void deleteDeck(MouseEvent event) {
		if(this.deckToLoad != null) {
			try {
				File deckToDelete = new File(this.deckToLoad);
				if (deckToDelete.exists()) {
					deckToDelete.delete();
				}
			} catch (Exception e) {
				System.out.println("Failed to load deck for delete");
			}
		}
		this.displaySavedDecks();
	}
 	
	public void clearSavedDeckButtons() {
		for (Object button : this.deckListBox.getChildren()) {
			((GenericButton)button).unHighlightButton();
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
	
	public void closeWindow() {
		Node source = this.rootPane;
		Stage stage = (Stage)source.getScene().getWindow();
		stage.close();
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