package net.zomis.cardshifter.ecs.effects;

import java.util.function.*;

import com.cardshifter.modapi.base.Component;
import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSSystem;
import com.cardshifter.modapi.base.Entity;
import com.cardshifter.modapi.cards.BattlefieldComponent;
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

    public <T extends IEvent> Function<Entity, ECSSystem> triggerSystemBefore(Class<T> eventClass, BiPredicate<Entity, T> interestingEvents, BiConsumer<Entity, T> handler) {
        return e -> new ECSSystem() {
            @Override
            public void startGame(ECSGame game) {
                game.getEvents().registerHandlerBefore(this, eventClass, this::event);
            }

            private void event(T event) {
                if (interestingEvents.test(e, event)) {
                    handler.accept(e, event);
                }
            }
        };
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
	public <T extends IEvent> EffectComponent giveSelf(Function<Entity, ECSSystem> system) {
		GameEffect effect = event -> event.getEntity().getGame().addSystem(new InGameSystem(event.getEntity(), system.apply(event.getEntity())));
		return new EffectComponent("Give target " + system, effect);
	}

	public EffectComponent toSelf(Consumer<Entity> effect) {
		return new EffectComponent(effect.toString(), event -> effect.accept(event.getEntity()));
	}

	public Component described(String description, EffectComponent effectComponent) {
		return new EffectComponent(description, effectComponent.getEffect());
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

}
