
package com.cardshifter.sandbox.helper;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
//		ExecutorService executorService = Executors.newSingleThreadExecutor();
//		Future<?> future = executorService.submit(() -> {
//			AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
//				runnable.run();
//				return null;
//			}, RESTRICTED_ACCESS_CONTROL_CONTEXT);
//		});
//		try {
//			future.get();
//		} catch (InterruptedException ex) {
//			Thread.currentThread().interrupt();
//		} catch (ExecutionException ex) {
//			throw (RuntimeException)ex.getCause();	//no checked exception can be passed in via AccessController.doPrivileged with PrivilegedAction<Void>
//			//TODO perhaps instead wrap and unwrap in unit tests
//		}
//		executorService.shutdown();
		runnable.run();
	}
}
