/**
 * Attempts to replicate the logic located in PhrancisGame.java
 * This module is to set up all the core game components required to start a game
 * Location of original Java file:
 *   cardshifter-core/src/main/java/net/zomis/cardshifter/ecs/usage/PhrancisGame.java 
 * @module PhrancisGame
 */

/** Card zones and owners */
var ZoneComponent = Java.type("com.cardshifter.modapi.cards.ZoneComponent");
/** Players & win/lose logic */
var PlayerComponent = Java.type("com.cardshifter.modapi.base.PlayerComponent");
/** Build decks using min/max size and cards */
var DeckConfigFactory = Java.type("net.zomis.cardshifter.ecs.config.DeckConfigFactory");
/** Potential configurations for players/entities. Primarily stores DeckConfigs */
var ConfigComponent = Java.type("net.zomis.cardshifter.ecs.config.ConfigComponent");
/** 
 * Enum that represents various player resources:
 * MAX_HEALTH, SNIPER, DOUBLE_ATTACK, TAUNT, DENY_COUNTERATTACK, HEALTH, MANA, MANA_MAX, SCRAP, 
 * ATTACK, MANA_COST, SCRAP_COST, ENCHANTMENTS_ACTIVE, SICKNESS, ATTACK_AVAILABLE
 * @implements ECSResource
*/
var PhrancisResources = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources");
/**
 * Stores resources for entities
 * @implements CopyableComponent
 */
var ECSResourceMap = Java.type("com.cardshifter.modapi.resources.ECSResourceMap");
/**
 * Store attributes for entities
 * @implements CopyableComponent
 */
var ECSAttributeMap = Java.type("com.cardshifter.modapi.attributes.ECSAttributeMap");
/**
 * Core class for starting a Cardshifter game
 * @implements ECSMod
 */
var baseMod = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame");
/** Card attributes */
var Attributes = Java.type("com.cardshifter.modapi.attributes.Attributes");

/**
 * Declare game configuration
 * @param {Object} game - Game configuration data
 */
function declareConfiguration(game) {
	var neutral = game.newEntity();
	/**
	 * @param {Object} owner - Owner of the ZoneComponent.
	 * @param {string} name - Name of the ZoneComponent.
	 */
	var zone = new ZoneComponent(neutral, "Cards");
	neutral.addComponent(zone);
	addCards(zone);
	
	/** Parameters related to DeckConfigFactory */
	var maxCardsPerType = 3;
	var minSize = 30;
	var maxSize = 30;
	
	/**
	 * Create playerComponent 0 & 1, i.e., Player1 & Player2
	 * Config a deck for each player
	 */
	for (var i = 0; i < 2; i++) {
		/**
		 * @param {Object} newEntity() - New game entity.
		 */
		var entity = game.newEntity();
		/**
		 * @param {int} index - Index of the playerComponent.
		 * @param {string} name - Name of the playerComponent.
		 */
		var playerComponent = new PlayerComponent(i, "Player" + (i+1));
		/**
		 * @param {Object} playerComponent - Add the playerComponent to the game entity.
		 */
		entity.addComponent(playerComponent);
		/**
		 * @param {int} minSize - Minimum number of cards per deck.
		 * @param {int} maxSize - Maximum number of cards per deck.
		 * @param {Object} getCards() - Card ownership from ZoneComponent class.
		 * @param {int} maxCardsPerType - Maximum card of each type per deck.
		 */
		var config = DeckConfigFactory.create(minSize, maxSize, zone.getCards(), maxCardsPerType);
		/**
		 * @param {Object} ConfigComponent() - Configuration for player entities.
		 */
		entity.addComponent(new ConfigComponent().addConfig("Deck", config));
	}
}

/**
 * @params {Object} zone - Zone to which to add cards & attributes to.
 */

function addCards(zone) {
	var entity = zone.getOwner().getGame().newEntity();
	/**
	 * @param {Object} entity - Entity to which resources are being mapped.
	*/
	ECSResourceMap.createFor(entity)
		.set(PhrancisResources.HEALTH, 5)
		.set(PhrancisResources.MAX_HEALTH, 5)
		.set(PhrancisResources.ATTACK, 1)
		.set(PhrancisResources.SCRAP, 3)
		.set(PhrancisResources.MANA_COST, 2)
		.set(PhrancisResources.SICKNESS, 1)
		.set(PhrancisResources.TAUNT, 1)
		.set(PhrancisResources.ATTACK_AVAILABLE, 1);
	/**
	 * @param {Object} entity - Entity to which attributes are being mapped.
	 * @param {string} name - Name of the attribute.
	 */
	ECSAttributeMap.createFor(entity).set(Attributes.NAME, "Testing Thing");
	var mod = new baseMod();
	/**
	 * @param {string} EffectComponent - Determine the effect/creature type of the entity.
	 *   Currently available: "Mech", "Bio"
	 */
	entity.apply(mod.creature("Mech"));
	/**
	 * @param {Object} entity - Entity to add to bottom of zone
	 */
	zone.addOnBottom(entity);
	return entity;
}
/**
 * @param {Object} game - Game configuration.
 */
function setupGame(game) {

}
