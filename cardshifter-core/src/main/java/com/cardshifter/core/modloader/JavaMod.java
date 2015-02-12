
package com.cardshifter.core.modloader;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

/**
 *
 * @author Frank van Heeswijk
 */
public class JavaMod extends LoadableMod {
	private ECSMod ecsMod;

	private ModURLClassLoader modUrlClassLoader;

	JavaMod(final Path modDirectory) throws ModNotLoadableException {
		super(modDirectory);
	}

	@Override
	protected void load0() throws ModNotLoadableException {
		try {
			Properties properties = ModLoaderHelper.getConfiguration(modDirectory);
			String jarName = properties.getProperty("jar");
			String entryPoint = properties.getProperty("entryPoint");

			Path jarPath = modDirectory.resolve(jarName);

			try {
				modUrlClassLoader = AccessController.doPrivileged((PrivilegedExceptionAction<ModURLClassLoader>)() -> new ModURLClassLoader(new URL[] { jarPath.toUri().toURL() }, getClass().getClassLoader()));
			} catch (PrivilegedActionException ex) {
				throw new ModNotLoadableException(ex);
			}			 
			Class<?> clazz = Class.forName(entryPoint, false, modUrlClassLoader);
			if (!ECSMod.class.isAssignableFrom(clazz)) {
				throw new ModNotLoadableException(clazz + " does not implement ECSMod");
			}
			this.ecsMod = (ECSMod)clazz.newInstance();
		} catch (Exception ex) {
			throw new ModNotLoadableException(ex);
		}
	}

	@Override
	protected void unload0() {
		try {
			modUrlClassLoader.close();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	@Override
	protected void createGame0(ECSGame game) {
		ecsMod.setupGame(game);
	}

	@Override
	protected void declareConfiguration0(ECSGame game) {
		ecsMod.declareConfiguration(game);
	}

}
