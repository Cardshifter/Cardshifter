package com.cardshifter.gdx.ui.zones;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.ZoneMessage;

public abstract class ZoneView {

    public ZoneView() {
    }

    public void apply(ZoneMessage message) {

    }

    public abstract Actor getActor();

    public void addCard(CardInfoMessage message) {

    }

    public void removeCard(int id) {

    }

}
