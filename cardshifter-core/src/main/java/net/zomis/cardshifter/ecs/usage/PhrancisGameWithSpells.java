package net.zomis.cardshifter.ecs.usage;

import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

import net.zomis.cardshifter.ecs.EntityCannotUseSystem;
import net.zomis.cardshifter.ecs.UntilEndOfOwnerTurnSystem;

import com.cardshifter.modapi.base.PlayerComponent;
import com.cardshifter.modapi.cards.DrawStartCards;
import com.cardshifter.modapi.cards.ZoneComponent;
import com.cardshifter.modapi.phase.PhaseEndEvent;
import com.cardshifter.modapi.players.Players;
import com.cardshifter.modapi.resources.ResourceRetriever;

public class PhrancisGameWithSpells extends PhrancisGameNewAttackSystem {
	
	@Override
	public void addCards(ZoneComponent zone) {
		super.addCards(zone);
		
		Filters filters = new Filters();
		Effects effects = new Effects();
		IntUnaryOperator attackLimit = i -> Math.max(i, 0);
		IntUnaryOperator healthLimit = i -> Math.max(i, 1);
		Supplier<FilterComponent> targetSupplier = () -> new FilterComponent(filters.isCreature().and(filters.isCreatureOnBattlefield()));
//		createTargetSpell(zone, 1, 3, effects.giveTarget(PhrancisResources.SNIPER, 1).and(effects.giveTarget(PhrancisResources.ATTACK, -2, attackLimit)),
//				targetSupplier.get());
		
		createTargetSpell(zone, 4, 2, effects.giveTarget(e -> new UntilEndOfOwnerTurnSystem(e, new EntityCannotUseSystem(e, PhrancisGame.ATTACK_ACTION))),
				new FilterComponent(new Filters().isCreatureOnBattlefield()));
		
		createTargetSpell(zone, 4, 4, effects.giveTarget(effects.triggerSystem(PhaseEndEvent.class,
				(me, event) -> Players.findOwnerFor(me) == event.getOldPhase().getOwner(),
				(me, event) -> DrawStartCards.drawCard(event.getOldPhase().getOwner()))),
				targetSupplier.get());
		
		ResourceRetriever health = ResourceRetriever.forResource(PhrancisResources.HEALTH);
		createSpell(zone, 0, 0, effects.forEach((src, dst) -> dst.hasComponent(PlayerComponent.class), (src, dst) -> health.resFor(dst).change(10)));
//		createTargetSpell(zone, 2, 2, effects.giveTarget(PhrancisResources.DOUBLE_ATTACK, 1).and(effects.giveTarget(PhrancisResources.ATTACK, -2, attackLimit)),
//				targetSupplier.get());
//		createTargetSpell(zone, 2, 2, effects.systemWhileOnBattlefield(e -> new HealAfterAttackSystem(e, PhrancisResources.HEALTH, PhrancisResources.MAX_HEALTH))
//				.and(effects.giveTarget(PhrancisResources.ATTACK, -2, attackLimit)),
//				targetSupplier.get());
		createTargetSpell(zone, 2, 2, effects.giveTarget(PhrancisResources.HEALTH, -2, healthLimit), targetSupplier.get());
		createTargetSpell(zone, 2, 2, effects.giveTarget(PhrancisResources.ATTACK, -2, attackLimit), targetSupplier.get());
		
//		createSpell(zone, 0, 0, effects.system(e -> new OpponentCannotUseSystem(e, ATTACK_ACTION)));
//		createSpell(zone, 2, 2, effects.giveTarget(new SpecificActionSystem(ATTACK_ACTION) {
//			@Override
//			protected void onPerform(ActionPerformEvent event) {
//				// Heals after each attack
////				if (event.getEntity())
//			}
//		}));
	}

}
