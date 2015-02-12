package com.cardshifter.gdx.ui.cards;

import com.cardshifter.api.outgoing.ZoneChangeMessage;
import com.cardshifter.gdx.ui.EntityView;
import com.cardshifter.gdx.ui.zones.ZoneView;

import java.util.Map;

public interface CardView extends EntityView {

    Map<String, Object> getInfo();

    void zoneMove(ZoneChangeMessage message, ZoneView destinationZone, CardView newCardView);
}
