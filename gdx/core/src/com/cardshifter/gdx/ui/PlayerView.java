package com.cardshifter.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.PlayerMessage;
import com.cardshifter.gdx.CardshifterGame;

public class PlayerView implements EntityView {

    private final int id;
    private final int index;
    private final Table table;

    public PlayerView(CardshifterGame game, PlayerMessage message) {
        this.table = new Table(game.skin);
        this.id = message.getId();
        this.index = message.getIndex();
        this.set(message);
    }

    public Actor getActor() {
        return table;
    }

    public void set(PlayerMessage message) {

    }

    public int getIndex() {
        return index;
    }

    @Override
    public void set(Object key, Object value) {

    }

    @Override
    public void remove() {

    }
}
