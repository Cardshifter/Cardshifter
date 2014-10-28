
package com.cardshifter.modapi.cards;

import java.util.Objects;

import com.cardshifter.modapi.base.Component;

/**
 *
 * @author Frank van Heeswijk
 */
public class CardImageComponent extends Component {
	private final String cardImage;
	
	public CardImageComponent(final String cardImage) {
		this.cardImage = Objects.requireNonNull(cardImage, "cardImage");
	}

	public String getCardImage() {
		return cardImage;
	}
}
