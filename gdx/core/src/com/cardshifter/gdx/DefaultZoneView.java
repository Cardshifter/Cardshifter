package com.cardshifter.gdx;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.ZoneMessage;
import com.cardshifter.gdx.ui.CardViewBig;
import com.cardshifter.gdx.ui.CardViewSmall;
import com.cardshifter.gdx.ui.ZoneView;

public class DefaultZoneView extends ZoneView {

    private final Table table;
    private final CardshifterGame game;

    public DefaultZoneView(CardshifterGame game, ZoneMessage message) {
        this.table = new Table(game.skin);
        this.table.add(message.getName() + '@' + message.getId()).row();
        this.game = game;
    }

    @Override
    public void addCard(CardInfoMessage message) {
        CardViewSmall view = new CardViewSmall(this.game, message);
        table.add(view.getTable()).width(120).expandX().fill();
    }

    @Override
    public Actor getActor() {
        return table;
    }
}
