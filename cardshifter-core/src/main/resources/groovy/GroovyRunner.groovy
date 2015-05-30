import com.cardshifter.core.modloader.GroovyModInterface
import com.cardshifter.core.modloader.ECSModTest
import com.cardshifter.modapi.base.ECSGame
import org.codehaus.groovy.control.CompilerConfiguration

public class MyGroovyMod implements GroovyModInterface {

    String name
    GroovyMod groovyMod

    MyGroovyMod(File dir, String name, ClassLoader cl) {
        this.name = name
        File modDirectory = dir
        Binding binding = new Binding()
        this.groovyMod = new GroovyMod(loader: cl, modDirectory: modDirectory, binding: binding)
        File file = new File(modDirectory, "Game.groovy")
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.setScriptBaseClass(DelegatingScript.class.getName())
        GroovyShell sh = new GroovyShell(cl, binding, cc)
        DelegatingScript script = (DelegatingScript) sh.parse(file)
        script.setDelegate(groovyMod)
        script.run()
    }

    @Override
    void declareConfiguration(ECSGame game) {
        println 'declare config'
        groovyMod.declareConfiguration(game)
    }

    @Override
    void setupGame(ECSGame game) {
        println 'setup game'
        groovyMod.setupGame(game)
    }

    @Override
    List<ECSModTest> getTests() {
        List<ECSModTest> result = new ArrayList<>();
        Binding binding = new Binding()
        def file = new File(groovyMod.modDirectory, "test.groovy")
        if (!file.exists()) {
            return null
        }

        def delegate = new TestDelegate(tests: result)
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.setScriptBaseClass(DelegatingScript.class.getName())
        GroovyShell sh = new GroovyShell(groovyMod.loader, binding, cc)
        DelegatingScript script = (DelegatingScript) sh.parse(file)
        script.setDelegate(delegate)
        script.run()
        return result
    }

}



new MyGroovyMod(dir, name, cl)