package net.zomis.cardshifter.ecs.usage;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.BattlefieldComponent;
import com.cardshifter.modapi.cards.CardComponent;
import com.cardshifter.modapi.cards.Cards;
import com.cardshifter.modapi.cards.ZoneChangeEvent;
import com.cardshifter.modapi.events.EntityRemoveEvent;
import com.cardshifter.modapi.events.IEvent;
import com.cardshifter.modapi.resources.ECSResource;
import com.cardshifter.modapi.resources.ECSResourceData;
import com.cardshifter.modapi.resources.ResourceRetriever;


public class Effects {

	public <T extends IEvent> Function<Entity, ECSSystem> triggerSystem(Class<T> eventClass, BiPredicate<Entity, T> interestingEvents, BiConsumer<Entity, T> handler) {
		return e -> new ECSSystem() {
			@Override
			public void startGame(ECSGame game) {
				game.getEvents().registerHandlerAfter(this, eventClass, this::event);
			}
			
			private void event(T event) {
				if (interestingEvents.test(e, event)) {
					 handler.accept(e, event);
				}
			}
		};
	}
	
	public EffectComponent forEach(TargetFilter filter, TargetEffect consumer) {
		GameEffect effect = event -> event.getEntity().getGame()
			.findEntities(target -> filter.isTargetable(event.getEntity(), target))
			.forEach(target -> consumer.perform(event.getEntity(), target));
		return new EffectComponent("For each " + filter + ", " + consumer, effect);
	}
	
	public EffectComponent scrapAll() {
		GameEffect effect = event -> event.getEntity().getGame()
			.findEntities(e -> e.hasComponent(CardComponent.class) && Cards.isOnZone(e, BattlefieldComponent.class))
			.forEach(e -> forceScrap(e));
		return new EffectComponent("Scrap ALL Minions", effect);
	}
	
	private void forceScrap(Entity entity) {
		entity.getGame().findSystemsOfClass(ScrapSystem.class).forEach(sys -> sys.forceScrap(entity));
	}

	public EffectComponent giveTarget(ECSResource resource, int value) {
		ResourceRetriever res = ResourceRetriever.forResource(resource);
		GameEffect effect = event -> event.getAction().getAllTargets().forEach(e -> res.resFor(e).change(value));
		return new EffectComponent("Give target " + value + " " + resource, effect);
	}

	public EffectComponent giveTarget(ECSResource resource, int value, IntUnaryOperator operator) {
		ResourceRetriever res = ResourceRetriever.forResource(resource);
		GameEffect effect = event -> event.getAction().getAllTargets().forEach(e -> {
			ECSResourceData data = res.resFor(e);
			data.change(value);
			data.set(operator.applyAsInt(data.get()));
		});
		return new EffectComponent("Give target " + value + " " + resource, effect);
	}

	public <T extends IEvent> EffectComponent giveTarget(Function<Entity, ECSSystem> system) {
		GameEffect effect = event -> event.getAction().getAllTargets().forEach(e -> e.getGame().addSystem(new InGameSystem(e, system.apply(e))));
		return new EffectComponent("Give target " + system, effect);
	}

	public <T extends IEvent> EffectComponent giveSelf(Function<Entity, ECSSystem> system) {
		GameEffect effect = event -> event.getEntity().getGame().addSystem(new InGameSystem(event.getEntity(), system.apply(event.getEntity())));
		return new EffectComponent("Give target " + system, effect);
	}

	public static class InGameSystem implements ECSSystem {

		private final Entity owningEntity;
		private final ECSSystem systemToRemove;

		public InGameSystem(Entity owningEntity, ECSSystem system) {
			this.owningEntity = owningEntity;
			this.systemToRemove = system;
		}

		@Override
		public void startGame(ECSGame game) {
			game.getEvents().registerHandlerAfter(systemToRemove, EntityRemoveEvent.class, this::removeCheck);
			game.getEvents().registerHandlerAfter(systemToRemove, ZoneChangeEvent.class, this::removeCheck);
			game.addSystem(systemToRemove);
		}
		
		private void removeCheck(EntityRemoveEvent event) {
			if (event.getEntity() == owningEntity) {
				event.getEntity().getGame().removeSystem(systemToRemove);
			}
		}
		
		private void removeCheck(ZoneChangeEvent event) {
			if (event.getDestination() instanceof BattlefieldComponent) {
				return;
			}
			if (event.getCard() == owningEntity) {
				event.getCard().getGame().removeSystem(systemToRemove);
			}
		}
	}

	public EffectComponent systemWhileOnBattlefield(Function<Entity, ECSSystem> systemSupplier) {
		GameEffect effect = event -> event.getAction().getAllTargets().forEach(e -> e.getGame().addSystem(new InGameSystem(e, systemSupplier.apply(e))));
//		GameEffect effect = e -> e.getEntity().getGame().addSystem(new InGameSystem(e.getEntity(), systemSupplier.apply(e.getEntity())));
		
		return new EffectComponent("special system", effect);
	}

	public EffectComponent addSystem(Function<Entity, ECSSystem> systemSupplier) {
		GameEffect effect = e -> e.getEntity().getGame().addSystem(systemSupplier.apply(e.getEntity()));
		
		return new EffectComponent("Add System", effect);
	}

}
