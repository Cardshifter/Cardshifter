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
 * Stores integer value resources for entities
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


function declareConfiguration(game) {
	neutral = game.newEntity();
	var zone = new ZoneComponent(neutral, "Cards");
	neutral.addComponent(zone);
	addCards(zone);
		
	var maxCardsPerType = 3;
	var minSize = 30;
	var maxSize = 30;
		
	for (var i = 0; i < 2; i++) {
		var entity = game.newEntity();
		var playerComponent = new PlayerComponent(i, "Player" + (i+1));
		entity.addComponent(playerComponent);
		var config = DeckConfigFactory.create(minSize, maxSize, zone.getCards(), maxCardsPerType);
		entity.addComponent(new ConfigComponent().addConfig("Deck", config));
	}
}

function addCards(zone) {
	var entity = zone.getOwner().getGame().newEntity();
	ECSResourceMap.createFor(entity)
		.set(PhrancisResources.HEALTH, 5)
		.set(PhrancisResources.MAX_HEALTH, 5)
		.set(PhrancisResources.ATTACK, 1)
		.set(PhrancisResources.SCRAP, 3)
		.set(PhrancisResources.MANA_COST, 2)
		.set(PhrancisResources.SICKNESS, 1)
		.set(PhrancisResources.TAUNT, 1)
		.set(PhrancisResources.ATTACK_AVAILABLE, 1);
	ECSAttributeMap.createFor(entity).set(Attributes.NAME, "Testing Thing");
	var mod = new baseMod();
	entity.apply(mod.creature("Mech"));
	zone.addOnBottom(entity);
	return entity;
}

function setupGame(game) {

}
