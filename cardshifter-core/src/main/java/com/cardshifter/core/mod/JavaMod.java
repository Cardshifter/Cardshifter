
package com.cardshifter.core.mod;

import java.nio.file.Path;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 *
 * @author Frank van Heeswijk
 */
public class JavaMod extends BaseMod {
    private final URLClassLoader urlClassLoader;
    private final ECSMod ecsMod;
    
	public JavaMod(final Path bootFile) throws ModNotLoadableException {
		super(bootFile);

        try {
            try {
                urlClassLoader = AccessController.doPrivileged((PrivilegedExceptionAction<URLClassLoader>)(() -> new URLClassLoader(new URL[] { bootFile.toUri().toURL() }, getClass().getClassLoader())));
            } catch (PrivilegedActionException ex) {
                throw new ModNotLoadableException(ex);
            }            
            Class<?> clazz = Class.forName("com.cardshifter.mod.java.ExampleGame", false, urlClassLoader);
            if (!ECSMod.class.isAssignableFrom(clazz)) {
                throw new ModNotLoadableException(clazz + " does not implement ECSMod");
            }
            this.ecsMod = (ECSMod)clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new ModNotLoadableException(ex);
        }
	}
    
	
	@Override
	public ECSGame createGame() {
		ECSGame ecsGame = new ECSGame();
        ecsMod.setupGame(ecsGame);
        return ecsGame;
	}
}
