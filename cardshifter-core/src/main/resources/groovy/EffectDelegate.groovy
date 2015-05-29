import com.cardshifter.modapi.actions.ActionPerformEvent
import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.DrawStartCards
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.players.Players
import com.cardshifter.modapi.resources.ECSResource
import net.zomis.cardshifter.ecs.usage.functional.EntityConsumer

import java.util.stream.Collectors

class EffectDelegate {

    StringBuilder description = new StringBuilder()
    List<Closure> closures = new ArrayList<>()

    final Object targets = new Object()

    def perform(Entity source, ActionPerformEvent event) {
        perform(source)
    }

    def perform(Entity source) {
        closures.each {
            it.call(source, source)
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
            return Players.getNextPlayer(Players.findOwnerFor(entity))
        }
        assert false
    }

    static def zoneLookup(Entity entity, String zoneName) {
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

            EffectDelegate[] deleg = new EffectDelegate[effects.length]
            for (int i = 0; i < deleg.length; i++) {
                deleg[i] = new EffectDelegate()
                Closure closure = effects[i]
                closure.setDelegate(deleg[i])
                closure.setResolveStrategy(Closure.DELEGATE_ONLY)
                closure.call()
                assert deleg[i].closures.size() > 0 : 'probability condition needs to have some actions'
            }
            String effectString = Arrays.stream(deleg).map({ef -> ef.description.toString()})
                .collect(Collectors.joining(' or '))
            description.append("Choose $count at random: " + effectString)
            closures.add({Entity source, Entity target ->
                List<EffectDelegate> list = new ArrayList<>(Arrays.asList(deleg))
                Collections.shuffle(list, source.game.random)
                for (int i = 0; i < count; i++) {
                    for (Closure act : list.get(i).closures) {
                        act.call(source, target)
                    }
                }
            })
        }]
    }

    def withProbability(double probability, @DelegatesTo(EffectDelegate) Closure action) {
        EffectDelegate deleg = new EffectDelegate()
        action.setDelegate(deleg)
        action.setResolveStrategy(Closure.DELEGATE_ONLY)
        action.call()
        assert deleg.closures.size() > 0 : 'probability condition needs to have some actions'
        description.append("$probability chance to $deleg.description")
        closures.add({Entity source, Entity target ->
            double random = source.game.random.nextDouble()
            println "random $random probability $probability perform ${random < probability}"
            if (random < probability) {
                println "Calling closures: $deleg.closures"
                for (Closure act : deleg.closures) {
                    act.call(source, target)
                }
            }
        })
    }

    def repeat(int count, @DelegatesTo(EffectDelegate) Closure action) {
        EffectDelegate deleg = new EffectDelegate()
        action.setDelegate(deleg)
        action.setResolveStrategy(Closure.DELEGATE_FIRST)
        action.call()
        assert deleg.closures.size() > 0 : 'repeat needs to have some actions'
        description.append("$deleg.description $count times")
        closures.add({Entity source, Entity target ->
            for (int i = 0; i < count; i++) {
                println "Calling closures: $deleg.closures"
                for (Closure act : deleg.closures) {
                    act.call(source, target)
                }
            }
        })
    }

    def drawCard(String who, int count) {
        def s = count == 1 ? '' : 's'
        if (who == 'all') {
            description.append("All players draw $count card$s\n")
            closures.add({Entity source, Entity target ->
                Players.getPlayersInGame(source.game).forEach({Entity e ->
                    for (int i = 0; i < count; i++) {
                        DrawStartCards.drawCard(e)
                    }
                })
            })
            return;
        }
        description.append("$who draw $count card$s\n")
        closures.add({Entity source, Entity target ->
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
            resource.retriever().resFor(target).change(value)
        }
        [to: {Object who -> targetedAction(action, who, "Heal $value to %who%")}]
    }

    def change(ECSResource resource) {
        [by: {int amount ->
            EntityConsumer action = {Entity source, Entity target ->
                resource.retriever.resFor(target).change(amount)
            }
            [onCards: {Object obj ->
                targetedAction(action, obj, "Change $resource by $amount on %who%")
            }]
        }]
    }

    def set(ECSResource resource) {
        [to: {int amount ->
            EntityConsumer action = {Entity source, Entity target ->
                resource.retriever.resFor(target).set(amount)
            }
            [onCards: {Object obj ->
                targetedAction(action, obj, "Set $resource to $amount on %who%")
            }]
        }]
    }

    private Object targetedAction(EntityConsumer action, Object who, String desc) {
        String targetStr = '';
        Closure closure = null;
        if (who instanceof String) {
            targetStr = who;
            closure = {Entity source, Entity target ->
                Entity entity = entityLookup(source, who as String)
                action.perform(source, entity)
            }
        } else if (who instanceof Integer) {
            return [random: {Closure filter ->
                FilterDelegate filterDelegate = FilterDelegate.fromClosure(filter)
                Closure randomizedAction = {Entity source, Entity target ->
                    List<Entity> targets = filterDelegate.findMatching(source)
                    int count = who as int
                    Collections.shuffle(targets, source.game.random)
                    println "Targeting $who random of $targets with $desc"
                    targets.stream().limit(count).forEachOrdered({Entity dst ->
                        action.perform(source, dst)
                    })
                }
                description.append(desc.replace('%who%', "$who random $filterDelegate.description"))
                description.append('\n')
                closures.add(randomizedAction)
            }]
        } else if (who instanceof Closure) {
            FilterDelegate filter = FilterDelegate.fromClosure(who as Closure)
            targetStr = filter.description
            closure = {Entity source, Entity target ->
                filter.findMatching(source).forEach({Entity entity ->
                    action.perform(source, entity)
                })
            }
        }
        assert closure : "$description: Unknown target $who"

        description.append(desc.replace('%who%', targetStr))
        description.append('\n')
        closures.add(closure)
    }

    def damage(int value) {
        EntityConsumer action = {Entity source, Entity target ->
            assert target : 'Invalid entity'
            assert value >= 0 : 'Value cannot be negative'
            ECSResource resource = target.game.resource('health')
            assert resource : 'health resource not found'
            resource.retriever().resFor(target).change(-value)
        }
        [to: {Object who -> targetedAction(action, who, "Deal $value damage to %who%")}]
    }

    // summon 2 of 'Bodyman' to owner zone Hand
    def summon(int count) { // TODO: Support other methods of getting count (random, EntityInt)
        [of: {String cardName ->
            [to: {String who ->
                [zone: {String zoneName ->
                    String desc = "Summon " + count + " " + cardName + " at " + who + " " + zoneName;

                    Closure closure = {Entity source, Entity target ->
                        Entity zoneOwner = entityLookup(source, who)
                        ZoneComponent zone = zoneLookup(zoneOwner, zoneName)
                        int amount = count // valueLookup(source, obj.count);
                        assert amount >= 0

                        def name = com.cardshifter.modapi.attributes.Attributes.NAME;
                        name = com.cardshifter.modapi.attributes.AttributeRetriever.forAttribute(name);

                        def neutral = source.game.findEntities({entity ->
                            ZoneComponent comp = entity.getComponent(com.cardshifter.modapi.cards.ZoneComponent.class)
                            return (comp != null) && comp.getName().equals("Cards")
                        })
                        assert neutral.size() == 1

                        Entity what = neutral.get(0).getComponent(com.cardshifter.modapi.cards.ZoneComponent.class)
                                .getCards().stream().filter({card ->
                            println "Checking " + card
                            return name.getFor(card).equals(cardName)
                        }).findAny().get()

                        for (int i = 0; i < amount; i++) {
                            zone.addOnBottom what.copy()
                        }
                    }

                    description.append(desc)
                    description.append('\n')
                    println 'Effect: ' + desc
                    closures.add(closure)
                }]
            }]
        }]
    }

}
