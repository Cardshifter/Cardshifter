package systems;

import SystemsDelegate
import com.cardshifter.modapi.cards.DrawStartCards
import com.cardshifter.modapi.phase.PerformerMustBeCurrentPlayer;

public class GeneralSystems {
    static def setup() {
        SystemsDelegate.metaClass.PerformerMustBeCurrentPlayer << {
            addSystem(new PerformerMustBeCurrentPlayer())
        }
        SystemsDelegate.metaClass.startCards << {int count ->
            addSystem(new DrawStartCards(count))
        }
    }
}
