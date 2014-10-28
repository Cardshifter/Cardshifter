
package com.cardshifter.modapi.cards;

import java.util.Objects;

import com.cardshifter.modapi.base.Component;

/**
 *
 * @author Frank van Heeswijk
 */
public class NameComponent extends Component {
	private final String name;
	
	public NameComponent(final String name) {
		this.name = Objects.requireNonNull(name, "name");
	}

	public String getName() {
		return name;
	}
}
