package com.cardshifter.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.zomis.cardshifter.ecs.actions.ActionComponent;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.ai.AIComponent;
import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.PlayerComponent;
import net.zomis.cardshifter.ecs.cards.BattlefieldComponent;
import net.zomis.cardshifter.ecs.cards.HandComponent;
import net.zomis.cardshifter.ecs.cards.ZoneComponent;
import net.zomis.cardshifter.ecs.events.GameOverEvent;
import net.zomis.cardshifter.ecs.phase.PhaseController;
import net.zomis.cardshifter.ecs.resources.ResourceRetriever;
import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources;

import com.cardshifter.ai.CardshifterAI;
import com.cardshifter.ai.CompleteIdiot;

import javafx.scene.layout.HBox;

public class FXMLGameController {
	
	@FXML private Pane anchorPane;
	@FXML private HBox opponentHandPane;
	@FXML private HBox playerHandPane;
	@FXML private HBox opponentBattlefieldPane;
	@FXML private HBox playerBattlefieldPane;
	@FXML private Label turnLabel;
	@FXML private Label gameOverLabel;
	@FXML private Label opponentLife;
	@FXML private Label opponentCurrentMana;
	@FXML private Label opponentTotalMana;
	@FXML private Label opponentScrap;
	@FXML private Label playerLife;
	@FXML private Label playerCurrentMana;
	@FXML private Label playerTotalMana;
	@FXML private Label playerScrap;
	
	private List<Group> opponentHandData;
	private List<Group> playerHandData;
	private List<Group> opponentBattlefieldData;
	private List<Group> playerBattlefieldData;
	
	public ECSAction nextAction;
   
	private final CardshifterAI opponent = new CompleteIdiot();
	private ECSGame game;

	private PhaseController phases;
	private final ResourceRetriever health = ResourceRetriever.forResource(PhrancisResources.HEALTH);
	private final ResourceRetriever mana = ResourceRetriever.forResource(PhrancisResources.MANA);
	private final ResourceRetriever manaMax = ResourceRetriever.forResource(PhrancisResources.MANA_MAX);
	private final ResourceRetriever scrap = ResourceRetriever.forResource(PhrancisResources.SCRAP);
	
	private boolean gameHasStarted = false; // hack to make the buttons work properly

	private AIComponent aiComponent;
	private boolean aiIsLoaded;
	
	public void acceptAIChoice(AIComponent aiChoice) {
		this.aiComponent = aiChoice;
		this.aiIsLoaded = false;
	}
	
	public void initializeGame() {
		game = PhrancisGame.createGame(new ECSGame());
		phases = ComponentRetriever.singleton(game, PhaseController.class);
		
		if (!aiIsLoaded) {
			getPlayer(1).addComponent(this.aiComponent);
			this.aiIsLoaded = true;
		}
	}
	
	@FXML private void startGameButtonAction(ActionEvent event) {
		if (gameHasStarted) {
			return;
		}
		
		game.startGame();
		game.getEvents().registerHandlerAfter(this, GameOverEvent.class, e -> gameOverLabel.setVisible(true));
		gameHasStarted = true;
		turnLabel.setText(String.format("%d", phases.getRecreateCount()));
			
		this.playerBattlefieldData = new ArrayList<>();
		this.opponentBattlefieldData = new ArrayList<>();
		this.playerHandData = new ArrayList<>();
		this.opponentHandData = new ArrayList<>();
			
		this.createData();
		this.render();
	}
	
	@FXML private void newGameButtonAction(ActionEvent event) {
		this.initializeGame();
		
		gameOverLabel.setVisible(false);
		
		game.startGame();
		gameHasStarted = true;
		turnLabel.setText(String.format("%d", phases.getRecreateCount()));
			
		this.playerBattlefieldData = new ArrayList<>();
		this.opponentBattlefieldData = new ArrayList<>();
		this.playerHandData = new ArrayList<>();
		this.opponentHandData = new ArrayList<>();
			
		this.createData();
		this.render();
	}
	
	@FXML private void handleTurnButtonAction(ActionEvent event) {
		if (!gameHasStarted) {
			return;
		}
		
		Entity player = phases.getCurrentEntity();
		if (player == null) {
			// dirty fix for making it work with Mulligan action
			Set<Entity> players = game.getEntitiesWithComponent(PlayerComponent.class);
			for (Entity pl : players) {
				ActionComponent actions = pl.getComponent(ActionComponent.class);
				ECSAction action = actions.getAction("Mulligan");
				if (action != null) {
					action.perform(pl);
				}
			}
			
			this.createData();
			this.render();
			return;
		}
		ECSAction endturnAction = player.getComponent(ActionComponent.class).getAction("End Turn");
		endturnAction.perform(player);

		//This is the AI doing the turn action
		final Entity aiPlayer = getPlayer(1);
		while (phases.getCurrentEntity() == aiPlayer) {
			ECSAction action = opponent.getAction(aiPlayer);
			if (action == null) {
				System.out.println("Warning: Opponent did not properly end turn");
				break;
			}
			action.perform(aiPlayer);
		}

		turnLabel.setText(String.format("%d", phases.getRecreateCount()));

		//reload data at the start of a new turn
		this.createData();
		this.render();
	}

	/**
	 * RENDER - eventually change this to an Update method
	 */
	public void render() {
		// TODO: Create a fixed time step and render in that
		//this is a hack to make the buttons disappear when the choice is made
		//will not work with a fixed time step
		Node choiceBoxPane = anchorPane.lookup("#choiceBoxPane");
		anchorPane.getChildren().remove(choiceBoxPane);
		
		this.renderHands();
		this.renderBattlefields();
		
		this.updateGameLabels();
	}
	
	private void renderHands() {
		this.renderOpponentHand();
		this.renderPlayerHand();
	}
	
	private void renderOpponentHand() {
		opponentHandPane.getChildren().clear();
		opponentHandPane.getChildren().addAll(opponentHandData);
	}
	
	private void renderPlayerHand() {
		playerHandPane.getChildren().clear();
		playerHandPane.getChildren().addAll(playerHandData);
	}
	
	private void renderBattlefields() {
		this.renderOpponentBattlefield();
		this.renderPlayerBattlefield();
	}
	
	private void renderOpponentBattlefield() {
		opponentBattlefieldPane.getChildren().clear();
		opponentBattlefieldPane.getChildren().addAll(opponentBattlefieldData);
	}
	
	private void renderPlayerBattlefield() {
		playerBattlefieldPane.getChildren().clear();
		playerBattlefieldPane.getChildren().addAll(playerBattlefieldData);
	}
	
	/**
	 * Called at the end of turn, and when a card is played
	 */
	public void createData() {
		this.createHands();
		this.createBattlefields();
	}
	
	private void createHands() {
		this.createOpponentHand();
		this.createPlayerHand();
	}
	
	//CREATE OPPONENT CARD BACKS
	private void createOpponentHand() {
		opponentHandData.clear();
		
		//Opponent cards are rendered differently because the faces are not visible
		double paneHeight = opponentHandPane.getHeight();
		double paneWidth = opponentHandPane.getWidth();
		
		int numCards = this.getOpponentCardCount();
		int maxCards = Math.max(numCards, 10);
		double cardWidth = paneWidth / maxCards;
		
		int currentCard = 0;
		while (currentCard < numCards) {
			Group cardGroup = new Group();
			opponentHandData.add(cardGroup);
			
			Rectangle cardBack = new Rectangle(0,0,cardWidth,paneHeight);
			cardBack.setFill(Color.AQUAMARINE);
			cardGroup.getChildren().add(cardBack);
			
			currentCard++;
		}
	}
	
	private int getOpponentCardCount() {
		Entity player = getPlayer(1);
		ZoneComponent hand = player.getComponent(HandComponent.class);
		List<Entity> cardsInHand = hand.getCards();
		return cardsInHand.size();
	}
	
	private Entity getPlayer(int index) {
		return game.findEntities(entity -> entity.hasComponent(PlayerComponent.class) && entity.getComponent(PlayerComponent.class).getIndex() == index).get(0);
	}

	//CREATE PLAYER HAND
	private void createPlayerHand() {
		playerHandData.clear();
		
		List<Entity> cardsInHand = this.getCurrentPlayerHand();
		int numCards = cardsInHand.size();

		int cardIndex = 0;
		for (Entity card : cardsInHand) {
			CardNode cardNode = new CardNode(playerHandPane, numCards, "testName", card, this);
			Group cardGroup = cardNode.getCardGroup();
			cardGroup.setId(String.format("card%d", card.getId()));
			playerHandData.add(cardGroup);
			
			cardIndex++;
		}
	}
	
	private List<Entity> getCurrentPlayerHand() {
		Entity player = getPlayer(0);
		ZoneComponent hand = player.getComponent(HandComponent.class);
		return hand.getCards();
	}
	
	private void createBattlefields() {
		this.createOpponentBattlefield();
		this.createPlayerBattlefield();
	}
	
	private void createOpponentBattlefield() {
		opponentBattlefieldData.clear();
		
		List<Entity> cardsInBattlefield = this.getBattlefield(getPlayer(1));
		int numCards = cardsInBattlefield.size();

		int cardIndex = 0;
		for (Entity card : cardsInBattlefield) {
			CardNodeBattlefield cardNode = new CardNodeBattlefield(opponentBattlefieldPane, numCards, "testName", card, this, false);
			Group cardGroup = cardNode.getCardGroup();
			cardGroup.setId(String.format("card%d", card.getId()));
			opponentBattlefieldData.add(cardGroup);
			
			cardIndex++;
		} 
	}
	
	private void createPlayerBattlefield() {
		playerBattlefieldData.clear();
		
		List<Entity> cardsInBattlefield = this.getBattlefield(getPlayer(0));
		int numCards = cardsInBattlefield.size();

		int cardIndex = 0;
		for (Entity card : cardsInBattlefield) {
			CardNodeBattlefield cardNode = new CardNodeBattlefield(playerBattlefieldPane, numCards, "testName", card, this, true);
			Group cardGroup = cardNode.getCardGroup();
			cardGroup.setAutoSizeChildren(true); //NEW
			cardGroup.setId(String.format("card%d", card.getId()));
			//cardGroup.setTranslateX(cardIndex * cardNode.getWidth());
			playerBattlefieldData.add(cardGroup);
			
			cardIndex++;
		} 
	}
	
	private List<Entity> getBattlefield(Entity player) {
		ZoneComponent battlefield = player.getComponent(BattlefieldComponent.class);
		return battlefield.getCards();
	}
	
	//CHOICE BOX PANE
	public void buildChoiceBoxPane(Entity card, List<ECSAction> actionList) {
		Pane choiceBoxPane = new Pane();
		choiceBoxPane.setPrefHeight(367);
		choiceBoxPane.setPrefWidth(550);
		choiceBoxPane.setTranslateX(326);
		choiceBoxPane.setTranslateY(150);
		choiceBoxPane.setId("choiceBoxPane");
		
		choiceBoxPane.getChildren().clear();
		
		int numChoices	= actionList.size();
		double paneHeight = choiceBoxPane.getPrefHeight();
		double paneWidth = choiceBoxPane.getPrefWidth();
		double choiceBoxWidth = paneWidth / numChoices;
		
		int actionIndex = 0;
		for (ECSAction action : actionList) {
			ChoiceBoxNode choiceBox = new ChoiceBoxNode(choiceBoxWidth, paneHeight, "testName", action, this);
			Group choiceBoxGroup = choiceBox.getChoiceBoxGroup();
			choiceBoxGroup.setTranslateX(actionIndex * choiceBoxWidth);
			choiceBoxPane.getChildren().add(choiceBox.getChoiceBoxGroup());
			
			actionIndex++;
		}
		
		anchorPane.getChildren().add(choiceBoxPane);
	}
   
	// TARGETING
	public void performNextAction(Entity target) {
		nextAction.getTargetSets().get(0).addTarget(target);
		nextAction.perform(getPlayer(0));
		this.createData();
		this.render();
	}
	
	public void markTargets(List<Entity> targets) {
		List<Node> cardsInPlayerBattlefield = playerBattlefieldPane.getChildren();
		List<Node> cardsInOpponentBattlefield = opponentBattlefieldPane.getChildren();
		for (Entity target : targets) {
			for (Node node : cardsInPlayerBattlefield) {
				if (node.getId().equals(String.format("card%d", target.getId()))) {
					CardNodeBattlefield actionNode = (CardNodeBattlefield)node;
					actionNode.createTargetButton();
				}
			}
			for (Node node : cardsInOpponentBattlefield) {
				if (node.getId().equals(String.format("card%d", target.getId()))) {
					CardNodeBattlefield actionNode = (CardNodeBattlefield)node;
					actionNode.createTargetButton();
				}
			}
		}
	}
	
	//GAME STATE LABELS
	private void updateGameLabels() {
		Entity localOpponent = getPlayer(1);
		opponentLife.setText(String.valueOf(health.getFor(localOpponent)));
		opponentCurrentMana.setText(String.valueOf(mana.getFor(localOpponent)));
		opponentTotalMana.setText(String.valueOf(manaMax.getFor(localOpponent)));
		opponentScrap.setText(String.valueOf(scrap.getFor(localOpponent)));
		
		Entity player = getPlayer(0);
		playerLife.setText(String.valueOf(health.getFor(player)));
		playerCurrentMana.setText(String.valueOf(mana.getFor(player)));
		playerTotalMana.setText(String.valueOf(manaMax.getFor(player)));
		playerScrap.setText(String.valueOf(scrap.getFor(player)));
	}
	
	public Entity getPlayerPerspective() {
		return getPlayer(0);
	}

	void shutdown() {
//		aiExecutor.shutdownNow();
	}

}
