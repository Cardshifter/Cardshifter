
package com.cardshifter.sandbox.helper;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

/**
 *
 * @author Frank van Heeswijk
 */
public final class ModSandbox {
	private static final PermissionCollection ALLOWED_PERMISSIONS = new Permissions();
	static {
		//add permissions for mods
	}
	private static final AccessControlContext RESTRICTED_ACCESS_CONTROL_CONTEXT = 
		new AccessControlContext(new ProtectionDomain[] { new ProtectionDomain(null, ALLOWED_PERMISSIONS)});
	
	private ModSandbox() {
		throw new UnsupportedOperationException();
	}
	
	public static void executeSandboxed(final Runnable runnable) {
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			runnable.run();
			return null;
		}, RESTRICTED_ACCESS_CONTROL_CONTEXT);
	}
}
