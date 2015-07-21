package com.cardshifter.core.groovy

import com.cardshifter.core.modloader.GroovyModInterface
import com.cardshifter.core.modloader.ECSModTest
import com.cardshifter.modapi.base.ECSGame
import org.codehaus.groovy.control.CompilerConfiguration

class GroovyRunner implements GroovyModInterface {

    final String name
    final File modDirectory
    private GroovyMod groovyMod
    final ClassLoader classLoader

    GroovyRunner(File dir, String name, ClassLoader cl) {
        this.name = name
        this.modDirectory = dir
        this.classLoader = cl
    }

    @Override
    void declareConfiguration(ECSGame game) {
        Binding binding = new Binding()
        this.groovyMod = new GroovyMod(loader: classLoader, modDirectory: modDirectory, binding: binding)

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
        groovyMod.setupGame(game)
    }

    @Override
    List<ECSModTest> getTests() {
        List<ECSModTest> result = new ArrayList<>();
        Binding binding = new Binding()
        def file = new File(modDirectory, "test.groovy")
        if (!file.exists()) {
            return null
        }

        def delegate = new TestDelegate(tests: result, mod: this)
        CompilerConfiguration cc = new CompilerConfiguration()
        cc.setScriptBaseClass(DelegatingScript.class.getName())
        GroovyShell sh = new GroovyShell(classLoader, binding, cc)
        DelegatingScript script = (DelegatingScript) sh.parse(file)
        script.setDelegate(delegate)
        script.run()
        return result
    }

    public CardDelegate getCardDelegate() {
        return groovyMod.cardDelegate
    }

    public GroovyMod getGroovyMod() {
        return groovyMod
    }
}
