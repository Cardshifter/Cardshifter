import com.cardshifter.core.modloader.ECSModTest
import com.cardshifter.modapi.base.ECSGame

class TestDelegate {

    List<ECSModTest> tests
    MyGroovyMod mod

    def from(Closure setup) {
        [test: {String name ->
            [using: {Closure test ->
                tests.add(new ECSModTest(name, {
                    doTest(setup, test)
                }))
            }]
        }]
    }

    def test(String name) {
        from null test name
    }

    def doTest(Closure setup, Closure test) {
        def mod = new MyGroovyMod(mod.modDirectory, mod.name, mod.classLoader)
        ECSGame game = new ECSGame()

        mod.declareConfiguration(game)
        mod.setupGame(game)
        game.startGame()

        def delegate = new TestCaseDelegate(game: game, cardDelegate: mod.getCardDelegate(), mod: mod.groovyMod)
        if (setup) {
            setup.setDelegate(delegate)
            setup.setResolveStrategy(Closure.DELEGATE_FIRST)
            setup.call()
        }
        test.setDelegate(delegate)
        test.setResolveStrategy(Closure.DELEGATE_FIRST)
        test.call()
        assert delegate.finished()
    }

}