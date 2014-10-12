
package com.cardshifter.core.modloader;

import com.cardshifter.sandbox.helper.ModSandbox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import com.cardshifter.modapi.base.ECSGame;
import com.cardshifter.modapi.base.ECSMod;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 *
 * @author Frank van Heeswijk
 */
public class JavaMod extends LoadableMod {
	private ECSMod ecsMod;
	
	private URLClassLoader urlClassLoader;
	
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
				urlClassLoader = AccessController.doPrivileged((PrivilegedExceptionAction<URLClassLoader>)() -> new URLClassLoader(new URL[] { jarPath.toUri().toURL() }, getClass().getClassLoader()));
			} catch (PrivilegedActionException ex) {
				throw new ModNotLoadableException(ex);
			}			 
			Class<?> clazz = Class.forName(entryPoint, false, urlClassLoader);
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
			urlClassLoader.close();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
	
	@Override
	protected ECSGame createGame0() {
		ECSGame ecsGame = new ECSGame();
		ModSandbox.executeSandboxed(() -> ecsMod.setupGame(ecsGame));
		return ecsGame;
	}
}
