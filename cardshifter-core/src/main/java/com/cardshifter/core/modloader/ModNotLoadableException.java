
package com.cardshifter.core.modloader;

/**
 *
 * @author Frank van Heeswijk
 */
public class ModNotLoadableException extends Exception {
    private static final long serialVersionUID = 6464564567864456345L;

    public ModNotLoadableException() {
        super();
    }

    public ModNotLoadableException(final String message) {
        super(message);
    }

    public ModNotLoadableException(final Throwable cause) {
        super(cause);
    }

    public ModNotLoadableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
