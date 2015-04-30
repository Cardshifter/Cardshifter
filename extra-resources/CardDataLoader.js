/**
 * This module is for loading card attributes and resources to the Cardshifter game server
 *   using ECSResourceMap and ECSAttributeMap. 
 * Resources and attributes are obtained from CardData.js module
 * 
 * @module CardAttributeLoader
 */
 
 /**
  * Needs implemented using Nashorn
  */
var cardLibrary = CardData.loadCardLibrary();

function mapCardData (cardLibrary) {
    for (var entityIndex in cardLibrary.entities) {
        var entity = zone.getOwner().getGame().newEntity();
        var value = undefined;
        
        value = entities[entityIndex].name;
        if (value === undefined) {
            ECSAttributeMap.createFor(entity).set(Attributes.NAME, "no name");
        } else {
            ECSAttributeMap.createFor(entity).set(Attributes.NAME, value);
        }
        
        value = entities[entityIndex].creature;
        if (value !== undefined) {
            var scrapValue = entities[entityIndex].scrap;
            if (value.toLowerCase() === "mech") {
                entity.apply(mod.creature("Mech"));
                ECSResourceMap.createFor(entity).set(Resources.SCRAP, scrapValue);
            } else if (value.toLowerCase() === "bio") {
                entity.apply(mod.creature("Bio"));
                ECSResourceMap.createFor(entity).set(Resources.SCRAP, 0);
            }
        }
        
        value = entities[entityIndex].manaCost;
        if (value === undefined) {
            ECSResourceMap.createFor(entity).set(Resources.MANA_COST, 0);
        } else {
            ECSResourceMap.createFor(entity).set(Resources.MANA_COST, value);
        }
    }
    zone.addOnBottom(entity);
}

mapCardData(cardLibrary);
