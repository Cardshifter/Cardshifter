package net.zomis.cardshifter.ecs.cards;

import net.zomis.cardshifter.ecs.base.ComponentRetriever;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.base.ECSSystem;
import net.zomis.cardshifter.ecs.base.PlayerComponent;

public class DrawStartCards implements ECSSystem {

	private final int count;

	public DrawStartCards(int i) {
		this.count = i;
	}

	@Override
	public void startGame(ECSGame game) {
		for (Entity entity : game.getEntitiesWithComponent(PlayerComponent.class)) {
			for (int i = 0; i < this.count; i++) {
				drawCard(entity);
			}
		}
	}

	private static final ComponentRetriever<DeckComponent> decks = ComponentRetriever.retreiverFor(DeckComponent.class);
	private static final ComponentRetriever<HandComponent> hands = ComponentRetriever.retreiverFor(HandComponent.class);
	private static final ComponentRetriever<CardComponent> card = ComponentRetriever.retreiverFor(CardComponent.class);
	
	public static void drawCard(Entity entity) {
		DeckComponent deck = decks.get(entity);
		HandComponent hand = hands.get(entity);
		if (deck.isEmpty()) {
			entity.getGame().getEvents().executePostEvent(new DrawCardFailedEvent(entity));
		}
		else {
			Entity cardToDraw = deck.getTopCard();
			CardComponent topCard = card.get(deck.getTopCard());
			entity.getGame().executeCancellableEvent(new DrawCardEvent(cardToDraw, entity, deck, hand), () -> topCard.moveToBottom(hand));
			
		}
		
	}

}
