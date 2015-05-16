import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.ECSSystem

class SystemsDelegate {
    ECSGame game

    def methodMissing(String name, args) {
        println 'Systems Unsupported method: ' + name
    }

    void addSystem(ECSSystem system) {
        game.addSystem(system)
    }

}