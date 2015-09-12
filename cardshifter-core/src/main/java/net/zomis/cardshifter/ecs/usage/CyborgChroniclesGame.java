package net.zomis.cardshifter.ecs.usage;

import com.cardshifter.modapi.resources.*;

@Deprecated
public class CyborgChroniclesGame {

    @Deprecated
	public enum CyborgChroniclesResources implements ECSResource {
		MAX_HEALTH, HEALTH, SCRAP, ATTACK, SCRAP_COST
	}

	public static final String PLAY_ACTION = "Play";
	public static final String ENCHANT_ACTION = "Enchant";
	public static final String ATTACK_ACTION = "Attack";
	public static final String SCRAP_ACTION = "Scrap";
	public static final String END_TURN_ACTION = "End Turn";
	public static final String USE_ACTION = "Use";
}
