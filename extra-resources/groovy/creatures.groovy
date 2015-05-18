import com.cardshifter.modapi.actions.*
import com.cardshifter.modapi.attributes.*

CardDelegate.metaClass.manaCost << {int value ->
    mana_cost(value)
}

CardDelegate.metaClass.health << {int value ->
    setResource('health', value)
    setResource('max_health', value)
}

CardDelegate.metaClass.denyCounterAttack << {
    DENY_COUNTERATTACK 1
}

CardDelegate.metaClass.flavor << {String value ->
    ECSAttributeMap.createOrGetFor(entity).set(Attributes.FLAVOR, value)
}

CardDelegate.metaClass.creature << {String type ->
    println "Creature set: $type"
    def entity = entity()
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
