package com.cardshifter.gdx.ui.zones;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.ZoneMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.ui.EntityView;
import com.cardshifter.gdx.ui.cards.CardView;
import com.cardshifter.gdx.ui.cards.CardViewHidden;
import com.cardshifter.gdx.ui.cards.CardViewSmall;

import java.util.Map;

public class DefaultZoneView extends ZoneView {

    private final Table table;
    private final CardshifterGame game;

    public DefaultZoneView(CardshifterGame game, ZoneMessage message, Map<Integer, EntityView> viewMap) {
        this.table = new Table(game.skin);
//        this.table.add(message.getName() + '@' + message.getId()).row();
        this.game = game;
        for (int id : message.getEntities()) {
            viewMap.put(id, addCard(new CardInfoMessage(message.getId(), id, null)));
        }
    }

    @Override
    public final CardView addCard(CardInfoMessage message) {
        if (message == null || message.getProperties() == null) {
            CardViewHidden view = new CardViewHidden(this.game);
            table.add(view.getTable()).spaceLeft(5).width(100).fill();
            return view;
        }
        else {
            CardViewSmall view = new CardViewSmall(this.game, message);
            table.add(view.getTable()).spaceLeft(5).width(100).fill();
            return view;
        }
    }

    @Override
    public void removeCard(int id) {
    }

    @Override
    public Actor getActor() {
        return table;
    }
}
