import com.cardshifter.core.modloader.GroovyModInterface
import com.cardshifter.core.modloader.ECSModTest
import com.cardshifter.modapi.base.ECSGame
import org.codehaus.groovy.control.CompilerConfiguration

public class MyGroovyMod implements GroovyModInterface {

    final String name
    final File modDirectory
    private final GroovyMod groovyMod
    private final ClassLoader classLoader

    MyGroovyMod(File dir, String name, ClassLoader cl) {
        this.name = name
        this.modDirectory = dir
        this.classLoader = cl
        Binding binding = new Binding()
        this.groovyMod = new GroovyMod(loader: cl, modDirectory: modDirectory, binding: binding)
    }

    @Override
    void declareConfiguration(ECSGame game) {
        File file = new File(modDirectory, "Game.groovy")
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.setScriptBaseClass(DelegatingScript.class.getName())
        GroovyShell sh = new GroovyShell(classLoader, groovyMod.binding, cc)
        DelegatingScript script = (DelegatingScript) sh.parse(file)
        script.setDelegate(groovyMod)
        script.run()
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