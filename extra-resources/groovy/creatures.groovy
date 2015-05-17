import com.cardshifter.modapi.actions.*
import com.cardshifter.modapi.attributes.*

CardDelegate.metaClass.manaCost << {int value ->
    mana_cost(value)
}

CardDelegate.metaClass.flavor << {String value ->
    ECSAttributeMap.createOrGetFor(entity).set(Attributes.FLAVOR, value)
}

CardDelegate.metaClass.creature << {String type ->
    println 'this is ' + this
    def entity = entity()
    println 'entity is ' + entity
    def actions = entity.getComponent(ActionComponent)
    def playAction = new ECSAction(entity, 'Play', {act -> true }, {act -> })
    def attackAction = new ECSAction(entity, 'Attack', {act -> true }, {act -> }).addTargetSet(1, 1)

    actions.addAction(playAction)
    actions.addAction(attackAction)

    entity.addComponent(new com.cardshifter.modapi.base.CreatureTypeComponent(type))

    sickness = 1
    taunt = 1
    attack_available = 1

}

"this is result from loading $CardDelegate"