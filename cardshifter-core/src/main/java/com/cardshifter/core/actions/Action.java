
package com.cardshifter.core.actions;

/**
 *
 * @author Frank van Heeswijk
 */
public interface Action {
	boolean isAllowed();
	
	void perform();
}
