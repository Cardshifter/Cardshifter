package com.cardshifter.ai;

import net.zomis.aiscores.FScorer;
import net.zomis.aiscores.ScoreConfigFactory;
import net.zomis.aiscores.scorers.PredicateScorer;
import net.zomis.aiscores.scorers.Scorers;
import net.zomis.aiscores.scorers.SimpleScorer;
import net.zomis.cardshifter.ecs.config.ConfigComponent;
import com.cardshifter.api.config.DeckConfig;
import net.zomis.cardshifter.ecs.usage.CyborgChroniclesGame;

import com.cardshifter.ai.phrancis.AttackAnalyze;
import com.cardshifter.modapi.actions.ECSAction;
import com.cardshifter.modapi.base.Entity;

public class AIs {
	
	private static FScorer<Entity, ECSAction> playActionScorer = new PredicateScorer<>(action -> action.getName().equals(CyborgChroniclesGame.PLAY_ACTION));

	public static ScoreConfigFactory<Entity, ECSAction> loser() {
		ScoreConfigFactory<Entity, ECSAction> config = new ScoreConfigFactory<>();
		config.withScorer(new PredicateScorer<>(action -> action.getName().equals(CyborgChroniclesGame.END_TURN_ACTION)));
		return config;
	}

	public static ScoreConfigFactory<Entity, ECSAction> idiot() {
		return new ScoreConfigFactory<>();
	}

	public static ScoreConfigFactory<Entity, ECSAction> medium() {
		ScoreConfigFactory<Entity, ECSAction> config = new ScoreConfigFactory<>();
		config.withScorer(new PredicateScorer<>(action -> action.getName().equals(CyborgChroniclesGame.USE_ACTION)), -10);
		config.withScorer(new PredicateScorer<>(action -> action.getName().equals(CyborgChroniclesGame.PLAY_ACTION)), 10);
		config.withScorer(new PredicateScorer<>(action -> action.getName().equals(CyborgChroniclesGame.ENCHANT_ACTION)), -10); // this AI does not enchant
		config.withScorer(new SimpleScorer<Entity, ECSAction>(AttackAnalyze::scrapScore));
		config.withScorer(new SimpleScorer<Entity, ECSAction>(AttackAnalyze::attackScore));
		return config;
	}

	public static ScoreConfigFactory<Entity, ECSAction> fighter() {
		ScoreConfigFactory<Entity, ECSAction> config = new ScoreConfigFactory<>();
		config.withScorer(Scorers.multiplication(playActionScorer, new SimpleScorer<Entity, ECSAction>(AttackAnalyze::health)), 10);
		config.withScorer(Scorers.multiplication(playActionScorer, new SimpleScorer<Entity, ECSAction>(AttackAnalyze::attack)), 2);
		config.withScorer(new PredicateScorer<>(action -> action.getName().equals(CyborgChroniclesGame.USE_ACTION)), -10);
		config.withScorer(new PredicateScorer<>(action -> action.getName().equals(CyborgChroniclesGame.SCRAP_ACTION)), -1);
		config.withScorer(Scorers.multiplication(new SimpleScorer<Entity, ECSAction>(AttackAnalyze::scrapNeeded), 
				new SimpleScorer<Entity, ECSAction>(AttackAnalyze::scrapIfCanGetKilled)));
//		config.withScorer(new SimpleScorer<>(AttackAnalyze::scrapScore));
		config.withScorer(new SimpleScorer<Entity, ECSAction>(AttackAnalyze::attackScore));
		config.withScorer(new SimpleScorer<Entity, ECSAction>(AttackAnalyze::enchantScore));
		return config;
	}

	public static void mediumDeck(Entity entity, ConfigComponent config) {
		DeckConfig deck = config.getConfig(DeckConfig.class);
		if (!cardExists(deck, "spareparts")) {
			evilMythosDeck(deck);
			return;
		}
		createDeckFullWith(deck, "spareparts", "gyrodroid", "the-chopper", "shieldmech", "robot-guard",
				"humadroid", "assassinatrix", "fortimech", "scout-mech", "supply-mech"); // Some Mechs
	}

	private static boolean cardExists(DeckConfig deck, String cardId) {
		return deck.getCardData().containsKey(cardId);
	}

	private static void createDeckFullWith(DeckConfig deck, String... ids) {
		if (deck == null) {
			return;
		}
		for (String id : ids) {
			if (deck.total() >= deck.getMaxSize()) {
				return;
			}
			deck.setChosen(id, deck.getMaxFor(id));
		}
	}

	public static void fighterDeck(Entity entity, ConfigComponent config) {
		DeckConfig deck = config.getConfig(DeckConfig.class);
		if (!cardExists(deck, "humadroid")) {
			evilMythosDeck(deck);
			return;
		}
		createDeckFullWith(deck, "humadroid", "fortimech", "upgrado-mk-i", "body-armor");
		deckAdd(deck, "robot-guard", "robot-guard", "scout-mech", "scout-mech", "supply-mech");
		deckAdd(deck, "vetter", "wastelander", "cyberpimp", "web-boss", "inside-man");
		deckAdd(deck, "reinforced-cranial-implants", "full-body-cybernetics-upgrade");
	}

	private static void evilMythosDeck(DeckConfig deck) {
		createDeckFullWith(deck, "ninja-spy", "monking", "poseidon", "tartarus", "zeus",
			"maitreya-buddha", "krishna", "healer", "yama", "slinger");
		deckAdd(deck, "guanyin", "archer", "archer", "varuna", "skeleton", "skeleton", "undead", "undead", "hades",
				"eight-immortals", "eight-immortals", "holy-man", "holy-man", "cronus", "shinje", "nuwa");
	}

	private static void deckAdd(DeckConfig deck, String... ids) {
		if (deck == null) {
			return;
		}
		for (String id : ids) {
			if (deck.total() >= deck.getMaxSize()) {
				return;
			}
			deck.add(id);
		}
	}
	
}
