/**
 * This module is for loading card attributes and resources to the Cardshifter game server
 *   using ECSResourceMap and ECSAttributeMap. 
 * Resources and attributes are obtained from CardData.js module
 * 
 * @module CardAttributeLoader
 */
 
var ZoneComponent = Java.type("com.cardshifter.modapi.cards.ZoneComponent");
var PlayerComponent = Java.type("com.cardshifter.modapi.base.PlayerComponent");
var DeckConfigFactory = Java.type("net.zomis.cardshifter.ecs.config.DeckConfigFactory");
var ConfigComponent = Java.type("net.zomis.cardshifter.ecs.config.ConfigComponent");
var PhrancisResources = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame.PhrancisResources");

/** 
 * Map that represents various card resources:
 * MAX_HEALTH, SNIPER, DOUBLE_ATTACK, TAUNT, DENY_COUNTERATTACK, HEALTH, MANA, MANA_MAX, SCRAP, 
 * ATTACK, MANA_COST, SCRAP_COST, ENCHANTMENTS_ACTIVE, SICKNESS, ATTACK_AVAILABLE
 */
var ECSResourceMap = Java.type("com.cardshifter.modapi.resources.ECSResourceMap");
/** 
 * Map that represents various card attributes:
 * NAME, FLAVOR
 */
var ECSAttributeMap = Java.type("com.cardshifter.modapi.attributes.ECSAttributeMap");
var baseMod = Java.type("net.zomis.cardshifter.ecs.usage.PhrancisGame");
var Attributes = Java.type("com.cardshifter.modapi.attributes.Attributes");


/**
 * Load cards from JSON (CONCEPT)
 * @param {Object} cardLibrary - JSON containing data about the available cards.
 * @param {Object} zone - Zone to which to add cards & attributes to.
 */
function mapCardData (cardLibrary) {
    "strict mode";
    for (let entityIndex in cardLibrary.entities) {
        let entity = zone.getOwner().getGame().newEntity();
        let value = undefined;
        
        /** ATTRIBUTES */
        
        value = entities[entityIndex].name;
        if (!value) {
            ECSAttributeMap.createFor(entity).set(Attributes.NAME, "no name");
        } else {
            ECSAttributeMap.createFor(entity).set(Attributes.NAME, value);
        }
        
        value = entities[entityIndex].flavor;
        if (!value) {
            ECSAttributeMap.createFor(entity).set(Attributes.FLAVOR, "");
        } else {
            ECSAttributeMap.createFor(entity).set(Attributes.FLAVOR, value);
        }
        
        /** CREATURE TYPES */
        
        value = entities[entityIndex].creature.toLowerCase();
        if (value) {
            let scrapValue = entities[entityIndex].scrap;
            if (value === "mech") {
                entity.apply(mod.creature("Mech"));
                ECSResourceMap.createFor(entity).set(Resources.SCRAP, scrapValue);
            } else if (value === "bio") {
                entity.apply(mod.creature("Bio"));
                ECSResourceMap.createFor(entity).set(Resources.SCRAP, 0);
            }
        }
        
        /** BASIC CARD VALUES **/
        
        value = entities[entityIndex].manaCost;
        if (value) {
            ECSResourceMap.createFor(entity).set(Resources.MANA_COST, value);
        }
        
        value = entities[entityIndex].health;
        if (value) {
            ECSResourceMap.createFor(entity).set(Resources.HEALTH, value);
        }
        
        value = entities[entityIndex].attack;
        if (value) {
            ECSResourceMap.createFor(entity).set(Resources.ATTACK, value);
        }
        
        value = entities[entityIndex].sickness;
        if (!value) {
            ECSResourceMap.createFor(entity).set(Resources.SICKNESS, 1);
        } else {
            ECSResourceMap.createFor(entity).set(Resources.SICKNESS, value);
        }
        
        value = entities[entityIndex].noAttack;
        if (!value) {
            ECSResourceMap.createFor(entity).set(Resources.ATTACK_AVAILABLE, 1);
        } else {
            ECSResourceMap.createFor(entity).set(Resources.ATTACK_AVAILABLE, 0);
        }
    }
    zone.addOnBottom(entity);
}

/**
 * Fetch external JSON file
 * (Needs implemented using Nashorn)
 */
var cardLibrary = CardData.loadCardLibrary();

/**  
 * Call card loader to map JSON values to ECS maps
 */
mapCardData(cardLibrary);
