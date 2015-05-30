import com.cardshifter.core.modloader.ECSModTest

class TestDelegate {

    List<ECSModTest> tests

    def from(Closure setup) {
        [test: {String name, Closure test ->
            tests.add(new ECSModTest(name, {
                setup.call()
                test.call()
            }))
        }]
    }

}