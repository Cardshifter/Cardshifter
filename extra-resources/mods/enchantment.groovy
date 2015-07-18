import com.cardshifter.modapi.actions.*
import com.cardshifter.modapi.attributes.*

cardExtension('addAttack') {int value ->
    attack value
}
cardExtension('addHealth') {int value ->
    health value
}
cardExtension('scrapCost') {int value ->
    scrap_cost value
}
cardExtension('health') {int value ->
    setResource('health', value)
    setResource('max_health', value)
}

cardExtension('enchantment') {
    def entity = entity()
    def actions = entity.getComponent(ActionComponent)
    def enchantAction = new ECSAction(entity, 'Enchant', {act -> true}, {act -> }).addTargetSet(1, 1)

    actions.addAction(enchantAction)
}

cardExtension('set') {resource, val ->
    def entity = entity()
    def eff = new net.zomis.cardshifter.ecs.effects.Effects();

    entity.addComponent(
        eff.described("Set " + resource + " to " + val,
            eff.giveTarget(resource, 1, {i -> val})
        )
    );
}

