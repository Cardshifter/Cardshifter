
package com.cardshifter.modapi.cards;

import java.util.Objects;

import com.cardshifter.modapi.base.Component;

/**
 *
 * @author Frank van Heeswijk
 */
public class CardTypeComponent extends Component {
	private final String cardType;
	
	public CardTypeComponent(final String cardType) {
		this.cardType = Objects.requireNonNull(cardType, "cardType");
	}

	public String getCardType() {
		return cardType;
	}
}
