import com.cardshifter.modapi.base.Entity
import com.cardshifter.modapi.cards.DrawStartCards
import com.cardshifter.modapi.cards.ZoneComponent
import com.cardshifter.modapi.players.Players
import com.cardshifter.modapi.resources.ECSResource

class EffectDelegate {

    StringBuilder description = new StringBuilder()
    List<Closure> closures = new ArrayList<>()

    def perform(Entity source, Entity target) {
        closures.each {
            it.call(source, target)
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

    def drawCard(String who, int count) {
        def s = count == 1 ? '' : 's'
        description.append("$who draw $count card$s\n")
        closures.add({Entity source, Entity target ->
            Entity drawer = entityLookup(source, who)
            DrawStartCards.drawCard(drawer)
        })
    }

    def heal(int value) {
        [to: {String who ->
            String desc = "Heal $who by $value"
            Closure closure = {Entity source, Entity target ->
                Entity entity = entityLookup(source, who)
                assert entity : 'Invalid entity'
                assert value >= 0 : 'Value cannot be negative'
                ECSResource resource = entity.game.resource('health')
                assert resource : 'health resource not found'
                resource.retriever().resFor(entity).change(value)
            }
            description.append(desc)
            description.append('\n')
            closures.add(closure)
        }]
    }

    def damage(int value) {
        [to: {String who ->
            String desc = "Damage $who by $value"
            Closure closure = {Entity source, Entity target ->
                Entity entity = entityLookup(source, who)
                assert entity : 'Invalid entity'
                assert value >= 0 : 'Value cannot be negative'
                ECSResource resource = entity.game.resource('health')
                assert resource : 'health resource not found'
                resource.retriever().resFor(entity).change(-value)
            }
            description.append(desc)
            description.append('\n')
            closures.add(closure)
        }]
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
