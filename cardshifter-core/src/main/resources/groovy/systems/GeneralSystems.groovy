package systems;

import SystemsDelegate
import com.cardshifter.modapi.cards.DamageConstantWhenOutOfCardsSystem
import com.cardshifter.modapi.cards.DrawCardAtBeginningOfTurnSystem
import com.cardshifter.modapi.cards.DrawCardEvent
import com.cardshifter.modapi.cards.DrawStartCards
import com.cardshifter.modapi.cards.LimitedHandSizeSystem
import com.cardshifter.modapi.cards.PlayEntersBattlefieldSystem
import com.cardshifter.modapi.cards.PlayFromHandSystem
import com.cardshifter.modapi.phase.PerformerMustBeCurrentPlayer
import com.cardshifter.modapi.resources.ECSResource
import net.zomis.cardshifter.ecs.usage.DestroyAfterUseSystem

import java.util.function.Consumer;

public class GeneralSystems {
    static def setup() {
        SystemsDelegate.metaClass.PerformerMustBeCurrentPlayer << {
            addSystem(new PerformerMustBeCurrentPlayer())
        }
        SystemsDelegate.metaClass.startCards << {int count ->
            addSystem(new DrawStartCards(count))
        }

    }

    static def cardSystems() {
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
        SystemsDelegate.metaClass.DamageConstantWhenOutOfCardsSystem << {ECSResource resource, int count ->
            addSystem new DamageConstantWhenOutOfCardsSystem(resource, count)
        }
        SystemsDelegate.metaClass.LimitedHandSizeSystem << {int limit, Consumer<DrawCardEvent> whenFull ->
            addSystem new LimitedHandSizeSystem(limit, whenFull)
        }
    }
}
