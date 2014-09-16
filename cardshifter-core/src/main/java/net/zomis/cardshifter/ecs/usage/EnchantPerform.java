package net.zomis.cardshifter.ecs.usage;

import java.util.Arrays;

import net.zomis.cardshifter.ecs.actions.ActionPerformEvent;
import net.zomis.cardshifter.ecs.actions.SpecificActionSystem;
import net.zomis.cardshifter.ecs.base.Entity;
import net.zomis.cardshifter.ecs.resources.ECSResource;
import net.zomis.cardshifter.ecs.resources.ResourceRetreiver;

public class EnchantPerform extends SpecificActionSystem {

	private final ECSResource[] resources;

	public EnchantPerform(ECSResource... enchantResources) {
		super("Enchant");
		this.resources = Arrays.copyOf(enchantResources, enchantResources.length);
	}

	@Override
	protected void onPerform(ActionPerformEvent event) {
		Entity target = event.getAction().getTargetSets().get(0).getTargets().get(0);
		Entity enchantment = event.getEntity();
		
		for (ECSResource resource : resources) {
			ResourceRetreiver res = ResourceRetreiver.forResource(resource);
			int enchantmentValue = res.getFor(enchantment);
			res.resFor(target).change(enchantmentValue);
		}
	}

}
