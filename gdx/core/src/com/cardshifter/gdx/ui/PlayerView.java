package com.cardshifter.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.PlayerMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.ui.res.ResViewFactory;
import com.cardshifter.gdx.ui.res.ResourceView;

import java.util.HashMap;

public class PlayerView implements EntityView {

    private final int id;
    private final int index;
    private final Table table;
    private final ResourceView resources;
    private final HashMap<String, Integer> properties;

    public PlayerView(CardshifterGame game, PlayerMessage message) {
        this.table = new Table(game.skin);
        this.id = message.getId();
        this.index = message.getIndex();
        ResViewFactory rvf = new ResViewFactory(game.skin);
        this.resources = rvf.forFormat(rvf.res("SCRAP"), rvf.str(" "), rvf.res("MANA"), rvf.str(" "), rvf.coloredRes("HEALTH", "MAX_HEALTH"));
        this.properties = new HashMap<String, Integer>(message.getProperties());
        this.table.add(message.getName()).row();
        this.table.add(this.resources.getActor());
        resources.update(properties);
    }

    public Actor getActor() {
        return table;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void set(Object key, Object value) {
        properties.put((String) key, (Integer) value);
        resources.update(properties);
    }

    @Override
    public void remove() {
        table.remove();
    }
}
