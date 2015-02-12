
package com.cardshifter.core.modloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 *
 * @author Frank van Heeswijk
 */
final class ModURLClassLoader extends URLClassLoader {
	ModURLClassLoader(final URL[] urls) {
		super(urls);
	}

	ModURLClassLoader(final URL[] urls, final ClassLoader parent) {
		super(urls, parent);
	}	

	ModURLClassLoader(final URL[] urls, final ClassLoader parent, final URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}

	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			int i = name.lastIndexOf('.');
			if (i != -1) {
				sm.checkPackageAccess(name.substring(0, i));
			}
		}
		return super.loadClass(name, resolve);
	}
}