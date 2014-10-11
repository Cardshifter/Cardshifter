package net.zomis.cardshifter.ecs.usage.cw2;

import net.zomis.cardshifter.ecs.usage.CW2Game;
import net.zomis.cardshifter.ecs.usage.EffectComponent;
import net.zomis.cardshifter.ecs.usage.CW2Game.Resources;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.resources.ECSResource;

public class CW2Effects {

	public EffectComponent damage(int i) {
		return new EffectComponent(e -> {});
	}

	public EffectComponent myRes(ECSResource wizards, int i) {
		return new EffectComponent(e -> {});
	}

	public Component oppRes(ECSResource res, int i) {
		return new EffectComponent(e -> {});
	}

	public Component allFocus(Resources crystals) {
		return new EffectComponent(e -> {});
	}

}
