package com.cardshifter.core.game;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.zomis.cardshifter.ecs.usage.PhrancisGame;
import net.zomis.cardshifter.ecs.usage.PhrancisGameNewAttackSystem;
import net.zomis.cardshifter.ecs.usage.PhrancisGameWithSpells;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.api.CardshifterConstants;
import com.cardshifter.modapi.ai.CardshifterAI;
import com.cardshifter.modapi.base.ECSMod;

/**
 * Class where the Mods and AIs are initialized.
 * 
 * @author Simon Forsberg
 */
public class ModCollection {

	/**
	 * All the AIs to initialize.
	 */
	private final Map<String, CardshifterAI> ais = new LinkedHashMap<>();
	/**
	 * All the mods to initialize.
	 */
	private final Map<String, ECSMod> mods = new HashMap<>();
	
	/**
	 * Initializes the AIs and Mods and puts them in the collections.
	 */
	public ModCollection() {
		ais.put("Loser", new ScoringAI(AIs.loser()));
		ais.put("Idiot", new ScoringAI(AIs.idiot()));
		ais.put("Medium", new ScoringAI(AIs.medium()));
		ais.put("Fighter", new ScoringAI(AIs.fighter()));
		
		mods.put(CardshifterConstants.VANILLA, new PhrancisGame());
		mods.put("New_Attack_Style", new PhrancisGameNewAttackSystem());
		mods.put("With spells", new PhrancisGameWithSpells());
	}
	
	/**
	 * Not yet implemented.
	 * 
	 * @param directory The directory to search for more mods.
	 */
	public void loadExternal(Path directory) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}
	
	/**
	 * 
	 * @return The CardshifterAI objects.
	 */
	public Map<String, CardshifterAI> getAIs() {
		return Collections.unmodifiableMap(ais);
	}
	
	/**
	 * 
	 * @return The ECSMod objects.
	 */
	public Map<String, ECSMod> getMods() {
		return Collections.unmodifiableMap(mods);
	}
	
}
