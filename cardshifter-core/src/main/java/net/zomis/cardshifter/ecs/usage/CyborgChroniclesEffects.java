package net.zomis.cardshifter.ecs.usage;

import net.zomis.cardshifter.ecs.effects.EffectComponent;
import net.zomis.cardshifter.ecs.effects.GameEffect;

import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.Cards;

public class CyborgChroniclesEffects {

	public EffectComponent scrapAll() {
		GameEffect effect = event -> event.getEntity().getGame()
			.findEntities(e -> e.hasComponent(CardComponent.class) && Cards.isOnZone(e, BattlefieldComponent.class))
			.forEach(e -> forceScrap(e));
		return new EffectComponent("Scrap ALL Minions", effect);
	}
	
	private void forceScrap(Entity entity) {
		entity.getGame().findSystemsOfClass(ScrapSystem.class).forEach(sys -> sys.forceScrap(entity));
	}

}
