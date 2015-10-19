package com.cardshifter.core.groovy

import com.cardshifter.modapi.actions.ActionComponent
import com.cardshifter.modapi.actions.ActionPerformEvent
import com.cardshifter.modapi.actions.ECSAction
import com.cardshifter.modapi.actions.UseCostSystem
import com.cardshifter.modapi.actions.attack.AttackDamageAccumulating
import com.cardshifter.modapi.actions.attack.AttackDamageHealAtEndOfTurn
import com.cardshifter.modapi.actions.attack.AttackOnBattlefield
import com.cardshifter.modapi.actions.attack.AttackSickness
import com.cardshifter.modapi.actions.attack.AttackTargetMinionsFirstThenPlayer
import com.cardshifter.modapi.actions.attack.TrampleSystem
import com.cardshifter.modapi.actions.enchant.EnchantPerform
import com.cardshifter.modapi.actions.enchant.EnchantTargetCreatureTypes
import com.cardshifter.modapi.ai.AIComponent
import com.cardshifter.modapi.attributes.Attributes
import com.cardshifter.modapi.base.Component
import com.cardshifter.modapi.base.ComponentRetriever
import com.cardshifter.modapi.base.CreatureTypeComponent
import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.ECSSystem
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.base.PlayerComponent
import com.cardshifter.modapi.cards.BattlefieldComponent
import com.cardshifter.modapi.cards.CardComponent
import com.cardshifter.modapi.cards.DeckComponent
import com.cardshifter.modapi.cards.DrawCardAtBeginningOfTurnSystem
import com.cardshifter.modapi.cards.DrawStartCards
import com.cardshifter.modapi.cards.HandComponent
import com.cardshifter.modapi.cards.MulliganSingleCards
import com.cardshifter.modapi.cards.PlayEntersBattlefieldSystem
import com.cardshifter.modapi.cards.PlayFromHandSystem
import com.cardshifter.modapi.cards.ZoneChangeEvent
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.events.EntityRemoveEvent
import com.cardshifter.modapi.events.IEvent
import com.cardshifter.modapi.phase.GainResourceSystem
import com.cardshifter.modapi.phase.PerformerMustBeCurrentPlayer
import com.cardshifter.modapi.phase.PhaseStartEvent
import com.cardshifter.modapi.phase.RestoreResourcesSystem
import com.cardshifter.modapi.players.Players
import com.cardshifter.modapi.resources.ECSResource
import com.cardshifter.modapi.resources.ResourceModifierComponent
import com.cardshifter.modapi.resources.RestoreResourcesToSystem
import net.zomis.cardshifter.ecs.effects.EffectActionSystem
import net.zomis.cardshifter.ecs.effects.EffectComponent
import net.zomis.cardshifter.ecs.effects.EffectTargetFilterSystem
import net.zomis.cardshifter.ecs.effects.Effects
import net.zomis.cardshifter.ecs.effects.EntityInt
import net.zomis.cardshifter.ecs.effects.FilterComponent
import net.zomis.cardshifter.ecs.effects.GameEffect
import net.zomis.cardshifter.ecs.effects.TargetFilter
import net.zomis.cardshifter.ecs.usage.ApplyAfterAttack
import net.zomis.cardshifter.ecs.usage.DestroyAfterUseSystem
import net.zomis.cardshifter.ecs.usage.ScrapSystem

import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.ToIntFunction
import java.util.function.UnaryOperator

class AttackSystemDelegate {
    ECSGame game

    def zone(String name) {
        assert name == 'Battlefield' // Only supported right now
        addSystem(new AttackOnBattlefield())
    }

    def cardsFirst(ECSResource resource) {
        addSystem new AttackTargetMinionsFirstThenPlayer(resource)
    }

    def sickness(ECSResource resource) {
        addSystem new AttackSickness(resource)
    }

    def accumulating(ECSResource attack, ECSResource health, BiPredicate<Entity, Entity> allowCounterAttack) {
        addSystem new AttackDamageAccumulating(attack, health, allowCounterAttack)
    }

    def healAtEndOfTurn(ECSResource health, ECSResource maxHealth) {
        addSystem new AttackDamageHealAtEndOfTurn(health, maxHealth)
    }

    def afterAttack(Predicate<Entity> condition, Consumer<Entity> apply) {
        addSystem new ApplyAfterAttack(condition, apply)
    }

    def trample(ECSResource resource) {
        addSystem new TrampleSystem(resource)
    }

    def addSystem(ECSSystem system) {
        game.addSystem(system)
    }
}

public class GeneralSystems {
    static UnaryOperator<Entity> whoPays(String str) {
        if (str.equals('owner') || str.equals('player')) {
            return {Entity e -> Players.findOwnerFor(e)}
        }
        if (str.equals('self')) {
            return {Entity e -> e}
        }
        throw new UnsupportedOperationException("Invalid value for whoPays: $str")
    }

    static def addEffect(def ent, Component effect) {
        Entity entity = (Entity) ent
        EffectComponent existing = entity.getComponent(EffectComponent);
        if (existing) {
            effect = existing.and(effect as EffectComponent)
        }
        entity.addComponent(effect)
    }

    static <T extends IEvent> void triggerBefore(Entity entity, String triggerId, Class<T> eventClass, BiPredicate<Entity, T> predicate, Closure closure) {
        EffectDelegate effect = EffectDelegate.create(closure, false)
        effect.description.triggerId = triggerId
        def eff = new Effects();
        addEffect(entity,
                eff.described(effect.description.toString(),
                        eff.giveSelf(
                                eff.triggerSystemBefore(eventClass,
                                        {Entity me, T event -> predicate.test(me, event)},
                                        {Entity source, T event -> effect.perform(source)}
                                )
                        )
                )
        )
    }

    static <T extends IEvent> void triggerAfter(Entity entity, String triggerId, Class<T> eventClass, BiPredicate<Entity, T> predicate, Closure closure) {
        EffectDelegate effect = EffectDelegate.create(closure, false)
        effect.description.triggerId = triggerId
        def eff = new Effects();
        addEffect(entity,
                eff.described(effect.description.toString(),
                        eff.giveSelf(
                                eff.triggerSystem(eventClass,
                                        {Entity me, T event -> predicate.test(me, event)},
                                        {Entity source, T event -> effect.perform(source)}
                                )
                        )
                )
        )
    }

    /**
     * @return A key into EffectDescription.vocabulary
     */
    private static getOnTurnTriggerId(String phase, String player) {
        def id = new StringBuilder();

        id.append('on')

        assert phase in ['start', 'end']
        id.append(phase.capitalize())

        id.append('Of')

        assert player in ['your', 'opponents', 'all']
        if (player == 'all') {
            id.append('Any')
        } else {
            id.append(player.capitalize())
        }

        id.append('Turn')

        return id.toString()
    }

    private static boolean ownerMatch(String str, Entity expected, Entity actual) {
        if (str == 'your') {
            return expected == actual
        } else if (str == 'opponent') {
            return expected != actual
        } else if (str == 'each') {
            return true
        }
        throw new IllegalArgumentException('Unexpected owner match: ' + str)
    }

    private static class SpellsDelegate {
        private Entity entity
        private ECSAction action

        private void addTargetSet(int min, int max, Closure filter) {
            action.addTargetSet(min, max)
            assert !entity.hasComponent(FilterComponent) : 'Only one target set is supported so far'
            FilterDelegate filterDelegate = FilterDelegate.fromClosure filter
            TargetFilter resultFilter = {Entity source, Entity target ->
                filterDelegate.predicate.test(source, target)
            }
            entity.addComponent(new FilterComponent(resultFilter))
        }

        def targets(Map map, Closure closure) {
            int min = map.getOrDefault('min', 0) as int
            int max = map.getOrDefault('max', Integer.MAX_VALUE) as int
            addTargetSet(min, max, closure)
        }

        def targets(int count) {
            def finalize = {Closure closure ->
                addTargetSet(count, count, closure)
            }
            [to: {int max -> [cards: finalize]},
                cards: finalize]
        }
    }

    static def setup(ECSGame game) {
        // this adds properties to entities so that it's possible to write `entity.name` to get the name (if there is one)
        game.getEntityMeta().getName << {Attributes.NAME.getOrDefault(delegate, null)}
        game.getEntityMeta().getFlavor << {Attributes.FLAVOR.getOrDefault(delegate, null)}
        game.getEntityMeta().getImagePath << {Attributes.IMAGE_PATH.getOrDefault(delegate, null)}
        game.getEntityMeta().getOwner << {Players.findOwnerFor(delegate)}
        game.getEntityMeta().getCard << {delegate.getComponent(CardComponent)}
        game.getEntityMeta().getDeck << {delegate.getComponent(DeckComponent)}
        game.getEntityMeta().getHand << {delegate.getComponent(HandComponent)}
        game.getEntityMeta().getBattlefield << {delegate.getComponent(BattlefieldComponent)}
        game.getEntityMeta().getZone << {
            // get the current zone of the card
            CardComponent card = delegate.getCard()
            return card ? card.currentZone : null
        }
        game.getEntityMeta().getCreatureType << {delegate.getComponent(CreatureTypeComponent)}
        game.getEntityMeta().getActions << {delegate.getComponent(ActionComponent)}
        game.getEntityMeta().getAi << {delegate.getComponent(AIComponent)}
        game.getEntityMeta().getPlayer << {delegate.getComponent(PlayerComponent)}
        game.getEntityMeta().getOpponent << {
            Players.getNextPlayer(Players.findOwnerFor(delegate as Entity))
        }
        game.getEntityMeta().drawCards << {Integer count ->
            for (int i = 0; i < count; i++) {
                DrawStartCards.drawCard(delegate as Entity)
            }
        }
        game.getEntityMeta().drawCard << {
            DrawStartCards.drawCard(delegate as Entity)
        }
        game.getEntityMeta().moveTo << {String name ->
            Entity e = delegate as Entity
            CardComponent card = e.getComponent(CardComponent)
            Collection<ZoneComponent> zones = card.owner.getSuperComponents(ZoneComponent)
            def zone = zones.find {it.name == name}
            card.moveToBottom(zone)
        }
        game.getEntityMeta().moveTo << {String owner, String name ->
            Entity e = delegate as Entity
            CardComponent card = e.getComponent(CardComponent)
            Entity zoneOwner = card.owner
            if (owner == 'opponent') {
                zoneOwner = Players.getNextPlayer(zoneOwner)
            } else if (owner == 'you') {
                zoneOwner = card.owner
            } else {
                throw new IllegalArgumentException("owner value $owner is not supported yet")
            }
            Collection<ZoneComponent> zones = zoneOwner.getSuperComponents(ZoneComponent)
            def zone = zones.find {it.name == name}
            card.moveToBottom(zone)
        }

        CardDelegate.metaClass.onEndOfTurn << {Closure closure ->
            onEndOfTurn('your', closure)
        }

        CardDelegate.metaClass.onEndOfTurn << {String turn, Closure closure ->
            triggerAfter((Entity) entity(), getOnTurnTriggerId('end', turn), PhaseStartEvent.class,
                    {Entity source, PhaseStartEvent event -> ownerMatch(turn, Players.findOwnerFor(source), event.getOldPhase().getOwner())}, closure)
        }

        CardDelegate.metaClass.onStartOfTurn << {Closure closure ->
            onStartOfTurn('your', closure)
        }

        CardDelegate.metaClass.onStartOfTurn << {String turn, Closure closure ->
            triggerAfter((Entity) entity(), getOnTurnTriggerId('start', turn), PhaseStartEvent.class,
                    {Entity source, PhaseStartEvent event -> ownerMatch(turn, Players.findOwnerFor(source), event.getNewPhase().getOwner())}, closure)
        }

        CardDelegate.metaClass.onDeath << {Closure closure ->
            triggerBefore((Entity) entity(), 'onDeath', EntityRemoveEvent.class,
                    {Entity source, EntityRemoveEvent event -> source == event.entity}, closure)
        }

        CardDelegate.metaClass.spell << {String actionName ->
            spell(actionName, null)
        }
        CardDelegate.metaClass.spell << {String actionName, Closure closure ->
            Entity entity = entity()
            def actions = entity.getComponent(ActionComponent)
            def useAction = new ECSAction(entity, actionName, {act -> true }, {act -> })
            actions.addAction(useAction)

            if (closure) {
                SpellsDelegate delegate = new SpellsDelegate(entity: entity, action: useAction)
                closure.setDelegate(delegate)
                closure.call()
            }
        }

        CardDelegate.metaClass.afterPlay << {Closure closure ->
            def eff = new net.zomis.cardshifter.ecs.effects.Effects();
            EffectDelegate effect = new EffectDelegate()
            closure.delegate = effect
            closure.setResolveStrategy(Closure.DELEGATE_FIRST)
            closure.call()
            GameEffect eventConsumer = {Entity ent, ActionPerformEvent event ->
                effect.perform(ent, event)
            } as GameEffect
            effect.description.triggerId = 'afterPlay'
            addEffect(entity(), new EffectComponent(effect.description.toString(), eventConsumer))
        }

        CardDelegate.metaClass.whilePresent << {Closure closure ->
            def eff = new Effects()
            WhilePresentDelegate effect = new WhilePresentDelegate()
            closure.delegate = effect
            closure.call()
            addEffect(entity(),
                eff.described("${effect.description}",
                    eff.toSelf({source ->
                        def resModifierObject = ComponentRetriever.singleton(source.game, ResourceModifierComponent)
                        def modifiers = effect.modifiers
                        for (def modifier in modifiers) {
                            resModifierObject.addModifier(modifier.resource, modifier.createModifier(source))
                        }
                    })
                )
            )
        }

        // Scrap
        SystemsDelegate.metaClass.EnchantTargetCreatureTypes << {String... args ->
            addSystem new EnchantTargetCreatureTypes(args)
        }
        SystemsDelegate.metaClass.EnchantPerform << {ECSResource... resources ->
            addSystem new EnchantPerform(resources)
        }
        SystemsDelegate.metaClass.ScrapSystem << {ECSResource resource, Predicate<Entity> predicate ->
            addSystem new ScrapSystem(resource, predicate)
        }


        // General
        SystemsDelegate.metaClass.PerformerMustBeCurrentPlayer << {
            addSystem(new PerformerMustBeCurrentPlayer())
        }
        SystemsDelegate.metaClass.startCards << {int count ->
            addSystem(new DrawStartCards(count))
        }
        SystemsDelegate.metaClass.attackSystem << {Closure clos ->
            clos.delegate = new AttackSystemDelegate(game: game)
            clos.call()
        }
        SystemsDelegate.metaClass.EffectActionSystem << {String name ->
            addSystem new EffectActionSystem(name)
        }
        SystemsDelegate.metaClass.targetFilterSystem << {String name ->
            addSystem new EffectTargetFilterSystem(name)
        }

        EffectDescription.setupStandardVocabulary()

    }

    static def resourceSystems() {
        SystemsDelegate.metaClass.gainResource << {Map map ->
            ECSResource resource = (ECSResource) map.get('res')
            int value = (int) map.get('value')
            int untilMax = (int) map.get('untilMax')
            EntityInt gain = {e -> Math.min(value, Math.max((int) 0, (int) untilMax - resource.getFor(e)))}

            addSystem(new GainResourceSystem(resource, gain))
        }
        SystemsDelegate.metaClass.restoreResources << {Map map ->
            ECSResource resource = (ECSResource) map.get('resource')
            Object value = map.get('value')
            EntityInt entityInt;
            if (value instanceof Closure) {
                entityInt = value
            } else if (value instanceof ECSResource) {
                entityInt = {Entity e -> (value as ECSResource).getFor(e)}
            } else {
                assert value instanceof Number
                entityInt = {e -> value}
            }
            assert resource
            assert entityInt
            addSystem new RestoreResourcesSystem(resource, entityInt)
        }

        SystemsDelegate.metaClass.upkeepCost << {Map map ->
            Predicate<Entity> filter = map.get('filter') as Predicate<Entity>
            ECSResource decreaseBy = map.get('decreaseBy') as ECSResource
            ECSResource decrease = map.get('decrease') as ECSResource
            addSystem new RestoreResourcesSystem(decrease, {Entity player ->
                List<Entity> entities = player.game.findEntities(filter);
                int sum = entities.stream().mapToInt({ Entity ent -> decreaseBy.getFor(ent) }).sum()
                return decrease.getFor(player) - sum
            })
        }

        SystemsDelegate.metaClass.effectOnSummon << {String zoneName ->
            assert zoneName
            addSystem {ECSGame game ->
                game.getEvents().registerHandlerAfter(this, ZoneChangeEvent, {ZoneChangeEvent event ->
                    if (event.source) {
                        return;
                    }
                    if (event.destination.name == zoneName) {
                        if (event.card.hasComponent(EffectComponent)) {
                            EffectComponent comp = event.card.getComponent(EffectComponent)
                            assert event.card.owner : 'Effect no owner for ' + event.card.debug()
                            comp.perform(event.card);
                        }
                    }
                })
            }
        }

        SystemsDelegate.metaClass.useCost << {Map map ->
//            (action: PLAY_ACTION, res: MANA, value: { res MANA_COST }, whoPays: "player")
            String action = map.get('action')
            ECSResource res = (ECSResource) map.get('res')
            Object value = map.get('value')
            String whoPaysStr = map.get('whoPays')
            ToIntFunction<Entity> entityInt = value instanceof ECSResource ?
                    {Entity e -> (value as ECSResource).getFor(e)} :
                    {Entity e -> (value as int).intValue()}
            UnaryOperator<Entity> whoPays = whoPays(whoPaysStr)
            assert whoPays : 'Who pays is null: ' + whoPaysStr
            addSystem new UseCostSystem(action, res, entityInt, whoPays)
        }
        SystemsDelegate.metaClass.RestoreResourcesToSystem << {Map map ->
            Predicate<Entity> filter = map.get('filter') as Predicate<Entity>
            ECSResource resource = map.get('resource') as ECSResource
            Object value = map.get('value')
            ToIntFunction<Entity> entityInt
            if (value instanceof Closure) {
                entityInt = value as ToIntFunction<Entity>
            } else {
                assert value instanceof Number
                entityInt = {e -> value}
            }
            addSystem new RestoreResourcesToSystem(filter, resource, entityInt)
        }
    }

    static def cardSystems(ECSGame game) {
        SystemsDelegate.metaClass.MulliganSingleCards << {
            addSystem new MulliganSingleCards(game)
        }
        SystemsDelegate.metaClass.playFromHand << {String zone ->
            addSystem(new PlayFromHandSystem(zone))
        }
        SystemsDelegate.metaClass.playEntersBattlefield << {String action ->
            addSystem(new PlayEntersBattlefieldSystem(action))
        }
        SystemsDelegate.metaClass.destroyAfterUse << {String action ->
            addSystem(new DestroyAfterUseSystem(action))
        }
        SystemsDelegate.metaClass.DrawCardAtBeginningOfTurnSystem << {
            addSystem new DrawCardAtBeginningOfTurnSystem()
        }
    }
}
