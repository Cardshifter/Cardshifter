package com.cardshifter.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.cardshifter.api.config.DeckConfig;
import net.zomis.cardshifter.ecs.usage.CardshifterIO;

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
	@FXML private AnchorPane deckInfoBox;
	@FXML private AnchorPane deckInfoButton;
	@FXML private VBox deckInfoLabelBox;
	
	private static final int CARDS_PER_PAGE = 12;
	private int currentPage = 0;
	private Map<Integer, CardInfoMessage> cardList = new HashMap<>();
	private List<List<CardInfoMessage>> pageList = new ArrayList<>();
	private DeckConfig activeDeckConfig;
	private CardInfoMessage cardBeingDragged;
	private Consumer<DeckConfig> configCallback;
	private String deckToLoad;
	private File deckDirectory;

	public void acceptDeckConfig(DeckConfig deckConfig, String modName, Consumer<DeckConfig> configCallback) {
		this.configCallback = configCallback;
		this.deckDirectory = new File("decks", modName);
		deckDirectory.mkdirs();
		this.activeDeckConfig = deckConfig;
		this.cardList = deckConfig.getCardData();
	}
	
	public void configureWindow() {
		this.previousPage.setOnMouseClicked(this::goToPreviousPage);
		this.nextPage.setOnMouseClicked(this::goToNextPage);
		this.exitButton.setOnMouseClicked(this::startGame);
		this.deckInfoButton.setOnMouseClicked(this::toggleDeckInfoBox);
		this.saveDeckButton.setOnMouseClicked(this::saveDeck);
		this.loadDeckButton.setOnMouseClicked(this::loadDeck);
		this.deleteButton.setOnMouseClicked(this::deleteDeck);
		this.clearDeckButton.setOnMouseClicked(this::clickedClearDeckButton);
		this.activeDeckBox.setOnDragDropped(e -> this.completeDragToActiveDeck(e, true));
		this.activeDeckBox.setOnDragOver(e -> this.completeDragToActiveDeck(e, false));
		
		List<CardInfoMessage> sortedCardList = new ArrayList<>(this.cardList.values());
		Collections.sort(sortedCardList, Comparator.comparingInt(msg -> msg.getId()));
		this.pageList = listSplitter(sortedCardList, CARDS_PER_PAGE);
		
		this.displaySavedDecks();
		this.clearDeck();
	}
	
	public void disableGameStart() {
		this.exitButton.setVisible(false);
	}
	
	private void startGame(MouseEvent event) {
		if (this.activeDeckConfig.total() == this.activeDeckConfig.getMaxSize()) {
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
			Label numberOfCardsLabel = new Label(String.format("%d / %d", numChosenCards, this.activeDeckConfig.getMaxFor(message.getId())));
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
		if (deckDirectory.listFiles() != null) {
			for (File file : deckDirectory.listFiles()) {
				try {
					if ((CardshifterIO.mapper().readValue(file, DeckConfig.class) instanceof DeckConfig)) {
						SavedDeckButton deckButton = new SavedDeckButton(this.deckListBox.getPrefWidth(), 40, cleanName(file.getName()), this);
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

	private String cleanName(String name) {
		final String extension = ".deck";
		if (name.endsWith(extension)) {
			return name.substring(0, name.length() - extension.length());
		}
		return name;
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
		this.cardCountLabel.setText(String.format("%d / %d", this.activeDeckConfig.total(), this.activeDeckConfig.getMaxSize()));
	}

	private void addCardToActiveDeck(MouseEvent event, CardInfoMessage message) {
		if (this.activeDeckConfig.total() < this.activeDeckConfig.getMaxSize()) {
			if(this.activeDeckConfig.getChosen().get(message.getId()) == null) {
				this.activeDeckConfig.setChosen(message.getId(), 1);
			} else {
				if (this.activeDeckConfig.getChosen().get(message.getId()) < this.activeDeckConfig.getMaxFor(message.getId())) {
					this.activeDeckConfig.add(message.getId());
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
	
	private void clickedClearDeckButton(MouseEvent event) {
		this.clearDeck();
	}
	
	private void toggleDeckInfoBox(MouseEvent event) {
		if (this.deckInfoBox.isVisible()) {
			this.deckInfoBox.setVisible(false);
		} else {
			this.deckInfoBox.setVisible(true);
			this.buildDeckInfoBox();
		}
	}
	
	private void buildDeckInfoBox() {
		this.deckInfoLabelBox.getChildren().clear();
		
		Map<Integer, Integer> manaCostValues = new HashMap<>();
		Map<Integer, Integer> scrapCostValues = new HashMap<>();
		Map<String, Integer> creatureTypes = new HashMap<>();
		
		for (int cardId : this.activeDeckConfig.getChosen().keySet()) {
			
			CardInfoMessage card = this.cardList.get(cardId);
			int cardCount = this.activeDeckConfig.getChosen().get(cardId);
			
			Optional.ofNullable(card.getProperties().get("creatureType"))
				.ifPresent(obj -> increase(creatureTypes, (String) obj, cardCount));
			Optional.ofNullable(card.getProperties().get("MANA_COST"))
				.ifPresent(obj -> increase(manaCostValues, (Integer) obj, cardCount));
			Optional.ofNullable(card.getProperties().get("SCRAP_COST"))
				.ifPresent(obj -> increase(scrapCostValues, (Integer) obj, cardCount));
		}
		
		for (int manaCost : manaCostValues.keySet()) {
			Label manaCostLabel = new Label();
			manaCostLabel.setText(String.format("Mana Cost = %d, Count = %d", manaCost, manaCostValues.get(manaCost)));
			this.deckInfoLabelBox.getChildren().add(manaCostLabel);
		}
		for (int scrapCost : scrapCostValues.keySet()) {
			Label scrapCostLabel = new Label();
			scrapCostLabel.setText(String.format("Scrap Cost = %d, Count = %d", scrapCost, scrapCostValues.get(scrapCost)));
			this.deckInfoLabelBox.getChildren().add(scrapCostLabel);
		}
		for (String creatureType : creatureTypes.keySet()) {
			Label creatureTypeLabel = new Label();
			creatureTypeLabel.setText(String.format("Creature Type %s, Count = %d", creatureType, creatureTypes.get(creatureType)));
			this.deckInfoLabelBox.getChildren().add(creatureTypeLabel);
		}
	}

	private <T> void increase(Map<T, Integer> map, T key, int cardCount) {
		map.merge(key, cardCount, (a, b) -> a + b);
	}

	private void clearDeck() {		
		this.activeDeckConfig.clearChosen();
		this.displayActiveDeck();
		this.displayCurrentPage();
	}
	
	private void saveDeck(MouseEvent event) {
		if(!this.deckNameBox.getText().isEmpty()) {
			try {
				File file = deckFile(deckNameBox.getText());
				if (file.isFile()) {
					System.out.println("Deck already exists");
				} else {
					CardshifterIO.mapper().writeValue(deckFile(deckNameBox.getText()), this.activeDeckConfig);
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
				this.activeDeckConfig = CardshifterIO.mapper().readValue(deckFile(this.deckToLoad), DeckConfig.class);
				String truncatedDeckName = this.deckToLoad.substring(0, this.deckToLoad.length()- 5);
				this.deckNameBox.setText(truncatedDeckName);
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
				File deckToDelete = deckFile(this.deckToLoad);
				System.out.println("deck " + deckToDelete + " exists? " + deckToDelete.exists());
				if (deckToDelete.exists()) {
					deckToDelete.delete();
				}
			} catch (Exception e) {
				System.out.println("Failed to load deck for delete");
			}
		}
		this.displaySavedDecks();
	}

	private File deckFile(String deckName) {
		return new File(deckDirectory, deckName + ".deck");
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