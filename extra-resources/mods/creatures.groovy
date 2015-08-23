import com.cardshifter.modapi.actions.*
import com.cardshifter.modapi.attributes.*

/* The cardExtension() values are taken from each card's definition (in their respective cardsets)
 * and added to the card entities at run time, i.e., when a game invitation is sent & accepted */

// Define creatures' manaCost value
cardExtension('manaCost') {int value ->
    mana_cost(value)
}
// Define creatures' health and max health values
cardExtension('health') {int value ->
    setResource('health', value)
    setResource('max_health', value)
}
// Define creatures' denyCounterAttack value
cardExtension('denyCounterAttack') {
    DENY_COUNTERATTACK 1
}
// Define creatures' flavor property
cardExtension('flavor') {String value ->
    ECSAttributeMap.createOrGetFor(entity).set(Attributes.FLAVOR, value)
}
/**
 * Define creatures' imagePath property, for use client-side to specify a partial or full directory path.
 * @example (partial path, preferred usage):
 *      imagePath "mods/mythos/zeus.jpg"
 * @example (full path, not recommended due to dependency on other web servers:
 *      imagePath "https://upload.wikimedia.org/wikipedia/commons/c/c8/Jupiter_Smyrna_Louvre_Ma13.jpg"
 */
cardExtension('imagePath') {String value ->
    ECSAttributeMap.createOrGetFor(entity).set(Attributes.IMAGE_PATH, value)
}

// Define creature entity and related actions and values
cardExtension('creature') {String type ->
    // Creatures must have a type
    assert type : 'Cannot use null creature type'
    def entity = entity()
    def actions = entity.getComponent(ActionComponent)
    def playAction = new ECSAction(entity, 'Play', {act -> true }, {act -> })
    def attackAction = new ECSAction(entity, 'Attack', {act -> true }, {act -> }).addTargetSet(1, 1)

    actions.addAction(playAction)
    actions.addAction(attackAction)

    entity.addComponent(new com.cardshifter.modapi.base.CreatureTypeComponent(type))
    // creatures have sickness by default, but this can be changed via each card's definition
    sickness 1
    // creatures can attack by default, but this can be changed via each card's definition
    attack_available 1

}
