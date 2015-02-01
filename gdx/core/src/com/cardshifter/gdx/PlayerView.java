package com.cardshifter.gdx;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.PlayerMessage;

public class PlayerView {

    private Table table;

    public PlayerView(CardshifterGame game) {
        this.table = new Table(game.skin);
    }

    public Actor getActor() {
        return table;
    }

    public void set(PlayerMessage message) {

    }
}
