package com.cardshifter.core.groovy

import com.cardshifter.modapi.actions.ActionPerformEvent
import com.cardshifter.modapi.actions.TargetSet
import com.cardshifter.modapi.attributes.AttributeRetriever
import com.cardshifter.modapi.attributes.Attributes
import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.DrawStartCards
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.players.Players
import com.cardshifter.modapi.resources.ECSResource
import net.zomis.cardshifter.ecs.usage.functional.EntityConsumer

import java.util.stream.Collectors

/**
 * Delegate for resolving effects.
 */
class EffectDelegate {

    static EffectDelegate create(Closure effects, boolean delegateOnly) {
        EffectDelegate delegate = new EffectDelegate()
        effects.setDelegate(delegate)
        effects.setResolveStrategy(delegateOnly ? Closure.DELEGATE_ONLY : Closure.DELEGATE_FIRST)
        effects.call()
        return delegate
    }

    /**
     * List of non-capitalized effect descriptions.
     */
    List<String> descriptionList = []

    /**
     * List of closures to perform, where each closure has the parameters (Entity, Object),
     * where the object *may* be an ActionPerformEvent, or null.
     */
    List<Closure> closures = new ArrayList<>()

    final Object targets = new Object()

    def perform(Entity source, ActionPerformEvent event) {
        closures.each {
            it.call(source, event)
        }
    }

    def perform(Entity source) {
        closures.each {
            it.call(source, null)
        }
    }

    static def entityLookup(Entity entity, String who) {
        if (who == 'owner') {
            return Players.findOwnerFor(entity)
        }
        if (who == 'you') {
            return Players.findOwnerFor(entity)
        }
        if (who == 'opponent') {
            def playerA = Players.findOwnerFor(entity)
            assert playerA : 'No owner found for ' + entity.debug()
            return Players.getNextPlayer(playerA)
        }
        assert false
    }

    static ZoneComponent zoneLookup(Entity entity, String zoneName) {
        def zone = entity.getSuperComponents(ZoneComponent).stream()
            .filter({it.getName().equals(zoneName)})
            .findAny().get()
        assert zone
        zone
    }

    def pick(int count) {
        [atRandom: {Closure... effects ->
            assert count > 0
            assert count <= effects.size()
            // { summon 1 of "Bar" to 'you' zone 'Battlefield' }

            EffectDelegate[] delegs = new EffectDelegate[effects.length]
            for (int i = 0; i < delegs.length; i++) {
                delegs[i] = create(effects[i], false)
                assert delegs[i].closures.size() > 0 : 'probability condition needs to have some actions'
            }

            // Build a bullet list
            String effectString = delegs.collect { deleg ->
                ' - ' + deleg.descriptionList.collect({it.capitalize()}).join('. ')
            }.join('\n')
            descriptionList << "choose $count at random:\n$effectString"

            closures.add({Entity source, Object data ->
                List<EffectDelegate> list = new ArrayList<>(Arrays.asList(delegs))
                Collections.shuffle(list, source.game.random)
                for (int i = 0; i < count; i++) {
                    for (Closure act : list.get(i).closures) {
                        act.call(source, data)
                    }
                }
            })
        }]
    }

    def withProbability(double probability, @DelegatesTo(EffectDelegate) Closure action) {
        EffectDelegate deleg = create(action, false)
        assert deleg.closures.size() > 0 : 'probability condition needs to have some actions'
        descriptionList.addAll(deleg.descriptionList.collect {"${probability * 100 as int}% chance to $it"})
        closures.add({Entity source, Object data ->
            double random = source.game.random.nextDouble()
            println "random $random probability $probability perform ${random < probability}"
            if (random < probability) {
                for (Closure act : deleg.closures) {
                    act.call(source, data)
                }
            }
        })
    }

    def doNothing() {
        descriptionList << "do nothing"
        closures.add({Entity source, Object data -> })
    }

    def repeat(int count, @DelegatesTo(EffectDelegate) Closure action) {
        EffectDelegate deleg = create(action, false)
        assert deleg.closures.size() > 0 : 'repeat needs to have some actions'

        def collector
        if (count == 1) {
            collector = {"$it once"}
        } else {
            collector = {"$it $count times"}
        }
        descriptionList.addAll(deleg.descriptionList.collect(collector))

        closures.add({Entity source, Object data ->
            for (int i = 0; i < count; i++) {
                for (Closure act : deleg.closures) {
                    act.call(source, data)
                }
            }
        })
    }

    def drawCard(String who, int count) {
        def s = count == 1 ? '' : 's'
        if (who == 'all') {
            descriptionList << "all players draw $count card$s"
            closures.add({Entity source, Object data ->
                Players.getPlayersInGame(source.game).forEach({Entity e ->
                    for (int i = 0; i < count; i++) {
                        DrawStartCards.drawCard(e)
                    }
                })
            })
            return;
        }
        descriptionList << "$who draws $count card$s"
        closures.add({Entity source, Object data ->
            Entity drawer = entityLookup(source, who)
            for (int i = 0; i < count; i++) {
                DrawStartCards.drawCard(drawer)
            }
        })
    }

    def heal(int value) {
        EntityConsumer action = {Entity source, Entity target ->
            assert target : 'Invalid entity'
            assert value >= 0 : 'Value cannot be negative'
            ECSResource resource = target.game.resource('health')
            assert resource : 'health resource not found'
            int max = target.max_health
            resource.retriever().resFor(target).changeBy(value, {i -> i >= max ? max : i})
        }
        targetedAction(action, "heal $value to %who%")
    }

    def damage(int value) {
        EntityConsumer action = {Entity source, Entity target ->
            assert target : 'Invalid entity'
            assert value >= 0 : 'Value cannot be negative'
            ECSResource resource = target.game.resource('health')
            assert resource : 'health resource not found'
            resource.retriever().resFor(target).change(-value)
        }
        targetedAction(action, "deal $value damage to %who%")
    }

    def change(ECSResource resource) {
        change([resource])
    }

    def change(Collection<ECSResource> resources) {
        [by: {int amount ->
            EntityConsumer action = {Entity source, Entity target ->
                resources.each {
                    it.retriever.resFor(target).change(amount)
                }
            }
            // No [brackets] around resources
            def resStr = resources.stream().map({it.toString()}).collect(Collectors.joining(", "))
            targetedAction(action, "change $resStr by $amount on %who%")
        }]
    }

    def set(ECSResource resource) {
        set([resource])
    }

    def set(Collection<ECSResource> resources) {
        [to: {int amount ->
            EntityConsumer action = {Entity source, Entity target ->
                resources.each {
                    it.retriever.resFor(target).set(amount)
                }
            }
            // No [brackets] around resources
            def resStr = String.join(", ", resources.stream().map({it.toString()}).collect(Collectors.toList()))
            targetedAction(action, "set $resStr to $amount on %who%")
        }]
    }

    private Object targetedAction(EntityConsumer action, String desc) {
        return [on: {Object who ->
            String targetStr = '';
            Closure closure = null;
            if (who == targets) {
                targetStr = 'targets'
                closure = {Entity source, Object data ->
                    assert data instanceof ActionPerformEvent
                    ActionPerformEvent event = data as ActionPerformEvent
                    assert event.action.getTargetSets().size() == 1
                    TargetSet targetSet = event.action.getTargetSets().get(0)
                    targetSet.chosenTargets.forEach({Entity target ->
                        action.perform(source, target)
                    })
                }
            } else if (who instanceof String) {
                targetStr = who;
                closure = {Entity source, Object data ->
                    Entity entity = entityLookup(source, who as String)
                    action.perform(source, entity)
                }
            } else if (who instanceof Integer) {
                return [random: {Closure filter ->
                    FilterDelegate filterDelegate = FilterDelegate.fromClosure(filter)
                    Closure randomizedAction = {Entity source, Object data ->
                        List<Entity> targets = filterDelegate.findMatching(source)
                        int count = who as int
                        Collections.shuffle(targets, source.game.random)
                        println "Targeting $who random of $targets with $desc"
                        targets.stream().limit(count).forEachOrdered({Entity dst ->
                            action.perform(source, dst)
                        })
                    }
                    descriptionList << desc.replace('%who%', "$who random $filterDelegate.description")
                    closures.add(randomizedAction)
                }]
            } else if (who instanceof Closure) {
                FilterDelegate filter = FilterDelegate.fromClosure(who as Closure)
                targetStr = filter.description
                closure = {Entity source, Object data ->
                    filter.findMatching(source).forEach({Entity entity ->
                        action.perform(source, entity)
                    })
                }
            }
            assert closure : "Effect must have a valid target, not '$who'. Description list: $descriptionList"

            descriptionList << desc.replace('%who%', targetStr)
            closures.add(closure)
        }]
    }



    // summon 2 of 'Bodyman' to owner zone Hand
    def summon(int count) { // TODO: Support other methods of getting count (random, EntityInt)
        [of: {String cardName ->
            assert cardName
            [to: {String who ->
                [zone: {String zoneName ->
                    String ownerName
                    if (who == "you") {
                        ownerName = "your"
                    } else {
                        ownerName = who + /'s/
                    }

                    Closure closure = {Entity source, Object data ->
                        Entity zoneOwner = entityLookup(source, who)
                        assert zoneOwner : "Could not find zoneOwner $who from ${source.debug()}"
                        ZoneComponent zone = zoneLookup(zoneOwner, zoneName)
                        int amount = count // valueLookup(source, obj.count);
                        assert amount >= 0

                        Entity what = cardModelByName(source.game, cardName)

                        for (int i = 0; i < amount; i++) {
                            zone.addOnBottom what.copy()
                        }
                    }

                    descriptionList << "summon $count $cardName to $ownerName $zoneName"
                    closures.add(closure)
                }]
            }]
        }]
    }

    def perish() {
        descriptionList << 'perish'
        closures.add({source, event -> source.destroy()})
    }

    def destroy(Object who) {
        EntityConsumer action = {Entity source, Entity target ->
            assert target : 'Invalid entity'
            target.destroy()
        }
        targetedAction(action, "destroy %who%").on(who)
    }

    static Entity cardModelByName(ECSGame game, String name) {
        def nameRetriever = AttributeRetriever.forAttribute(Attributes.NAME)
        def neutral = game.findEntities({entity ->
            ZoneComponent comp = entity.getComponent(ZoneComponent.class)
            return (comp != null) && comp.getName().equals("Cards")
        })
        assert neutral.size() == 1

        Entity result = neutral.get(0).getComponent(ZoneComponent.class)
                .getCards().stream().filter({card ->
            String match = nameRetriever.getOrDefault(card, null)
            return name.equals(match)
        }).findAny().orElseThrow { new IllegalArgumentException("No card found with name '$name'") }
        result
    }

}
