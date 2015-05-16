import com.cardshifter.modapi.base.ECSGame
import com.cardshifter.modapi.base.ECSMod
import org.codehaus.groovy.control.CompilerConfiguration

public class MyGroovyMod implements ECSMod {

    String name
    GroovyMod groovyMod

    MyGroovyMod(String name, ClassLoader cl) {
        this.name = name
        this.groovyMod = new GroovyMod(loader: cl)
        File file = new File("groovy/" + name + "/Game.groovy")
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.setScriptBaseClass(DelegatingScript.class.getName())
        GroovyShell sh = new GroovyShell(cl, new Binding(), cc)
        DelegatingScript script = (DelegatingScript) sh.parse(file)
        script.setDelegate(groovyMod)
        script.run()
    }

    void declareConfiguration(ECSGame game) {
        println 'declare config'
        groovyMod.declareConfiguration(game)
    }

    void setupGame(ECSGame game) {
        println 'setup game'
        groovyMod.setupGame(game)
    }

}



new MyGroovyMod(script, cl)