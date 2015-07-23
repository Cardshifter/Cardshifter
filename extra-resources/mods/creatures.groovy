import com.cardshifter.modapi.actions.*
import com.cardshifter.modapi.attributes.*

/**
 * The cardExtension() values are taken from each card's definition (in their respective cardsets)
 * and added to the card entities at run time, i.e., when a game invitation is sent & accepted
 */


cardExtension('manaCost') {int value ->
    mana_cost(value)
}

cardExtension('health') {int value ->
    setResource('health', value)
    setResource('max_health', value)
}

cardExtension('denyCounterAttack') {
    DENY_COUNTERATTACK 1
}

cardExtension('flavor') {String value ->
    ECSAttributeMap.createOrGetFor(entity).set(Attributes.FLAVOR, value)
}

cardExtension('creature') {String type ->
    assert type : 'Cannot use null creature type'
    def entity = entity()
    def actions = entity.getComponent(ActionComponent)
    def playAction = new ECSAction(entity, 'Play', {act -> true }, {act -> })
    def attackAction = new ECSAction(entity, 'Attack', {act -> true }, {act -> }).addTargetSet(1, 1)

    actions.addAction(playAction)
    actions.addAction(attackAction)

    entity.addComponent(new com.cardshifter.modapi.base.CreatureTypeComponent(type))

    sickness 1
    attack_available 1

}
