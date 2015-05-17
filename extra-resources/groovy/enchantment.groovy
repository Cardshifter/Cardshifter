import com.cardshifter.modapi.actions.*
import com.cardshifter.modapi.attributes.*

CardDelegate.metaClass.addAttack << {int value ->
    attack value
}
CardDelegate.metaClass.addHealth << {int value ->
    health value
}
CardDelegate.metaClass.scrapCost << {int value ->
    scrap_cost value
}

CardDelegate.metaClass.health << {int value ->
    setResource('health', value)
    setResource('max_health', value)
}

CardDelegate.metaClass.enchantment << {
    def entity = entity()
    def actions = entity.getComponent(ActionComponent)
    def enchantAction = new ECSAction(entity, 'Enchant', {act -> true}, {act -> }).addTargetSet(1, 1)

    actions.addAction(enchantAction)
}
