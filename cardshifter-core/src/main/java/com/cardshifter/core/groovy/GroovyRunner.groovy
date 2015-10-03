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
        def scriptRunner = new ScriptRunner(classLoader)
        this.groovyMod = new GroovyMod(modDirectory: modDirectory, scriptRunner: scriptRunner)

        File file = new File(modDirectory, "Game.groovy")
        scriptRunner.runScript(file, groovyMod)
        groovyMod.declareConfiguration(game)
    }

    @Override
    void setupGame(ECSGame game) {
        groovyMod.setupGame(game)
    }

    @Override
    List<ECSModTest> getTests() {
        List<ECSModTest> result = new ArrayList<>();

        def file = new File(modDirectory, "test.groovy")
        if (!file.exists()) {
            return null
        }

        def delegate = new TestDelegate(tests: result, mod: this)
        def scriptRunner = new ScriptRunner(classLoader)
        scriptRunner.runScript(file, delegate)

        return result
    }

    public CardDelegate getCardDelegate() {
        return groovyMod.cardDelegate
    }

    public GroovyMod getGroovyMod() {
        return groovyMod
    }
}

class ScriptRunner {

    final ClassLoader classLoader
    final Binding binding

    ScriptRunner(ClassLoader cl) {
        this.classLoader = cl
        this.binding = new Binding()
    }

    void runScript(File file, delegate) {
        CompilerConfiguration config = new CompilerConfiguration()
        config.setScriptBaseClass(DelegatingScript.class.getName())
        GroovyShell shell = new GroovyShell(classLoader, binding, config)
        DelegatingScript script = (DelegatingScript) shell.parse(file)
        script.setDelegate(delegate)
        script.run()
    }

}