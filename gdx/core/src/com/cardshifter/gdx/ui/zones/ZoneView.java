package com.cardshifter.gdx.ui.zones;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.ZoneMessage;
import com.cardshifter.gdx.ui.cards.CardView;

public abstract class ZoneView {

    public ZoneView() {
    }

    public void apply(ZoneMessage message) {

    }

    public abstract Actor getActor();

    public CardView addCard(CardInfoMessage message) {
        return null;
    }

    public void removeCard(int id) {

    }

}
