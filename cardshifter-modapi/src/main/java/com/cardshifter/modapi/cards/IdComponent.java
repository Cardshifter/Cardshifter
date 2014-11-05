
package com.cardshifter.modapi.cards;

import java.util.Objects;

import com.cardshifter.modapi.base.Component;

/**
 *
 * @author Frank van Heeswijk
 */
public class IdComponent extends Component {
	private final String id;
	
	public IdComponent(final String id) {
		this.id = Objects.requireNonNull(id, "id");
	}
	
	public String getId() {
		return id;
	}
}
