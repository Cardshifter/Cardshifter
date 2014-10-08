
package com.cardshifter.core.mod;

/**
 *
 * @author Frank van Heeswijk
 */
@FunctionalInterface
interface ModLoadingFunction<T, R, X extends Throwable> {
    R apply(final T t) throws X;
}
