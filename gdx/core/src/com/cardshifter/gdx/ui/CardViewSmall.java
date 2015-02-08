package com.cardshifter.gdx.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.EntityView;

public class CardViewSmall implements EntityView {

    private final Table table;
    private final Label effect;
    private final Label name;

    public CardViewSmall(CardshifterGame game, CardInfoMessage cardInfo) {
        table = new Table(game.skin);
        Gdx.app.log("CardView", "Creating for " + cardInfo.getProperties());
        table.defaults().expand();
        name = label(game, cardInfo, "name");
        table.add(name).colspan(2).width(100).row();
        // table.add(image);
        effect = label(game, cardInfo, "effect");
        table.add(effect).colspan(2).row();
        table.add("Cost").colspan(2).right().row();

        table.add(label(game, cardInfo, "creatureType")).left();
        table.add("Stats").right();
        table.setDebug(true, true);
    }

    public static Label label(CardshifterGame game, CardInfoMessage message, String key) {
        Label label = new Label(String.valueOf(message.getProperties().get(key)), game.skin);
        label.setEllipse(true);
        return label;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public void set(Object key, Object value) {

    }

    @Override
    public void remove() {

    }
}
