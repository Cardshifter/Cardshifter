package net.zomis.cardshifter.ecs.usage;

import net.zomis.cardshifter.ecs.actions.ActionComponent;
import net.zomis.cardshifter.ecs.actions.ECSAction;
import net.zomis.cardshifter.ecs.base.ECSGame;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.cards.DeckComponent;
import net.zomis.cardshifter.ecs.cards.DrawStartCards;
import net.zomis.cardshifter.ecs.cards.HandComponent;
import net.zomis.cardshifter.ecs.cards.RemoveDeadEntityFromZoneSystem;
import net.zomis.cardshifter.ecs.cards.ZoneComponent;
import net.zomis.cardshifter.ecs.components.PlayerComponent;
import net.zomis.cardshifter.ecs.phase.Phase;
import net.zomis.cardshifter.ecs.phase.PhaseController;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ECSResourceMap;
import net.zomis.cardshifter.ecs.resources.ResourceRetriever;
import net.zomis.cardshifter.ecs.systems.GainResourceSystem;
import net.zomis.cardshifter.ecs.systems.GameOverIfNoHealth;

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
		
		return game;
	}
	
}
