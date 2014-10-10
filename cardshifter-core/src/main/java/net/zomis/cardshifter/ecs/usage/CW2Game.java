package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.actions.ActionComponent;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.DeckComponent;
import com.cardshifter.modapi.cards.DrawStartCards;
import com.cardshifter.modapi.cards.HandComponent;
import com.cardshifter.modapi.cards.RemoveDeadEntityFromZoneSystem;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.phase.GainResourceSystem;
import com.cardshifter.modapi.phase.Phase;
import com.cardshifter.modapi.phase.PhaseController;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceMap;
import com.cardshifter.modapi.resources.GameOverIfNoHealth;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class CW2Game {

	public enum CWars2Res implements ECSResource {
		CASTLE, WALL;
	}
	public enum Resources implements ECSResource {
		BRICKS, WEAPONS, CRYSTALS;
		public Producers getProducer() {
			return Producers.values()[this.ordinal()];
		}
	}
	public enum Producers implements ECSResource {
		BUILDERS, RECRUITS, WIZARDS;
		public Resources getResource() {
			return Resources.values()[this.ordinal()];
		}
	}
	
	
	public static ECSGame createGame() {
		ECSGame game = new ECSGame();
		
		PhaseController phaseController = new PhaseController();
		game.newEntity().addComponent(phaseController);

		for (int i = 1; i <= 2; i++) {
			PlayerComponent playerComponent = new PlayerComponent(i - 1, "Player" + i);
			Entity player = game.newEntity().addComponent(playerComponent);
			Phase playerPhase = new Phase(player, "Main");
			phaseController.addPhase(playerPhase);
			
			ActionComponent actions = new ActionComponent();
			player.addComponent(actions);
			actions.addAction(new ECSAction(player, "Discard", act -> phaseController.getCurrentPhase() == playerPhase, act -> phaseController.nextPhase()).addTargetSet(1, 3));
			
			ECSResourceMap res = ECSResourceMap.createFor(player)
				.set(CWars2Res.CASTLE, 25)
				.set(CWars2Res.WALL, 15);
			
			for (Producers prod : Producers.values()) {
				res.set(prod, 2);
				res.set(prod.getResource(), 8);
			}
			
			ZoneComponent deck = new DeckComponent(player);
			ZoneComponent hand = new HandComponent(player);
			player.addComponents(hand, deck);
		}
		
//		UnaryOperator<Entity> owningPlayerPays = entity -> entity.getComponent(CardComponent.class).getOwner();
		for (Producers prod : Producers.values()) {
			game.addSystem(new GainResourceSystem(prod.getResource(), entity -> ResourceRetriever.forResource(prod).getFor(entity)));
		}
		
		
		
		// Actions - Play
//		game.addSystem(new PlayFromHandSystem(PLAY_ACTION));
//		game.addSystem(new UseCostSystem(PLAY_ACTION, PhrancisResources.MANA, manaCostResource::getFor, owningPlayerPays));
		
		// Draw cards
		game.addSystem(new DrawStartCards(8));
//		TODO: game.addSystem(new RecreateDeckSystem());
		
		// General setup
		game.addSystem(new GameOverIfNoHealth(CWars2Res.CASTLE));
		game.addSystem(new RemoveDeadEntityFromZoneSystem());
		game.addSystem(new CannotUseUnknownCards());
		
		return game;
	}
	
}
