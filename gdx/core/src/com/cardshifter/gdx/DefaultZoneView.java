package com.cardshifter.gdx;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.ZoneMessage;
import com.cardshifter.gdx.ui.ZoneView;

public class DefaultZoneView extends ZoneView {

    private final Table table;

    public DefaultZoneView(CardshifterGame game, ZoneMessage message) {
        this.table = new Table(game.skin);
    }

    @Override
    public void addCard(CardInfoMessage message) {

    }

    @Override
    public Actor getActor() {
        return table;
    }
}
