package com.cardshifter.modapi.actions.enchant;

import java.util.Arrays;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.actions.SpecificActionSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class EnchantPerform extends SpecificActionSystem {

	private final ECSResource[] resources;

	public EnchantPerform(ECSResource... enchantResources) {
		super("Enchant");
		this.resources = Arrays.copyOf(enchantResources, enchantResources.length);
	}

	@Override
	protected void onPerform(ActionPerformEvent event) {
		Entity target = event.getAction().getTargetSets().get(0).getChosenTargets().get(0);
		Entity enchantment = event.getEntity();
		
		for (ECSResource resource : resources) {
			ResourceRetriever res = ResourceRetriever.forResource(resource);
			int enchantmentValue = res.getFor(enchantment);
			res.resFor(target).change(enchantmentValue);
		}
		enchantment.destroy();
	}

}
