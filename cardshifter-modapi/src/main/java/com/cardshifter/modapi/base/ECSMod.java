package com.cardshifter.modapi.base;

public interface ECSMod {
	/**
	 * This method is called before asking each individual player to configure their options.
	 * 
	 * If functionality such as Deck-building is desired, this method needs to setup `PlayerComponents` to entities, and add a `ConfigComponent` to those entities
	 *   
	 * @param game The game to apply the mod to
	 */
	default void declareConfiguration(ECSGame game) {
		
	}
	
	/**
	 * This is the final method to be called in the game setup process. This should setup the game to be playable.
	 * @param game The game to setup
	 */
	void setupGame(ECSGame game);
}
