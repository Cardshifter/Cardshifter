import com.cardshifter.modapi.base.ECSGame

class SystemsDelegate {
    ECSGame game

    def methodMissing(String name, args) {
        println 'Unsupported method: ' + name
    }

}