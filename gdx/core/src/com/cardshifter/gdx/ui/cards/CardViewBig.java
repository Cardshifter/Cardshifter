package com.cardshifter.gdx.ui.cards;

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.ui.EntityView;

import java.util.HashMap;
import java.util.Map;

public class CardViewBig implements CardView {

    private final Table table;
    private final HorizontalGroup costs;
    private final HorizontalGroup gives;
    private final HashMap<String, Object> properties;

    public CardViewBig(CardshifterGame game, CardInfoMessage cardInfo) {
        this.properties = new HashMap<String, Object>(cardInfo.getProperties());
        table = new Table(game.skin);
        table.add((String) cardInfo.getProperties().get("name"));
        costs = new HorizontalGroup();
        costs.addActor(new Label("A", game.skin));
        table.add(costs).row();
        // table.add(image);
        Table textTable = new Table(game.skin);
        textTable.add("Abilities").row();
        textTable.add("Effect").row();
        textTable.add("Flavortext").bottom();
        table.add(textTable).colspan(2).row();
        table.add("Type").left();

        gives = new HorizontalGroup();
        gives.addActor(new Label("ABC", game.skin));
        table.add(gives).right();
    }

    public Table getTable() {
        return table;
    }

    @Override
    public void set(Object key, Object value) {
        
    }

    @Override
    public void remove() {
        table.remove();
    }

    @Override
    public Map<String, Object> getInfo() {
        return new HashMap<String, Object>(this.properties);
    }
}
