package com.cardshifter.modapi.base;

import com.cardshifter.modapi.events.IEvent;

import java.util.HashMap;
import java.util.Map;

public interface ECSMod {
	/**
	 * This method is called before asking each individual player to configure their options.
	 * 
	 * If functionality such as Deck-building is desired, this method needs to setup `PlayerComponents` to entities, and add a `ConfigComponent` to those entities
	 *   
	 * @param game
	 */
	default void declareConfiguration(ECSGame game) {

	}
	
	/**
	 * This is the final method to be called in the game setup process. This should setup the game to be playable.
	 * @param game The game to setup
	 */
	void setupGame(ECSGame game);

    /**
     * Returns the class that contains the DSL definitions.
     *
     * @return  The class that contains the DSL definitions.
     */
    default Class<? extends ModDSL> dslClass() {
        return null;
    }

	/**
	 * Returns a map that contains a mapping from event names to their corresponding classes.
	 *
	 * As example PhaseEndEvent would map to com.cardshifter.modapi.base.phase.PhaseEndEvent
	 *
	 * @return	A map that contains a mapping from event names to their corresponding classes.
	 */
	default Map<String, Class<?>> getEventMapping() {
		return new HashMap<>();
	}
}
