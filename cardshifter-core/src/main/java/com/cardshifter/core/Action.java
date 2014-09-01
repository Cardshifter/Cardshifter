
package com.cardshifter.core;

/**
 *
 * @author Frank van Heeswijk
 */
public interface Action {
	boolean isAllowed();
	
	void perform();
}
