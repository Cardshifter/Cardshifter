package com.cardshifter.modapi.cards;

import com.cardshifter.modapi.base.Entity;

public class DiscardComponent extends ZoneComponent {

    public DiscardComponent(Entity owner) {
        super(owner, "Discard");
        this.setGloballyKnown(true);
    }

}
