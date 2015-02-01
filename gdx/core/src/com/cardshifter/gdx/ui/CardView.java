package com.cardshifter.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.gdx.CardshifterGame;

/**
 * Created by Simon on 1/31/2015.
 */
public class CardView {

    private final Table table;
    private final HorizontalGroup costs;
    private final HorizontalGroup gives;

    public CardView(CardshifterGame game, CardInfoMessage cardInfo) {
        table = new Table(game.skin);
        table.add((String) cardInfo.getProperties().get("name"));
        costs = new HorizontalGroup();
        table.add(costs);
        table.row();
        // table.add(image);
        Table textTable = new Table(game.skin);
        textTable.add("Abilities").row();
        textTable.add("Effect description").row();
        textTable.add("Flavortext").bottom();
        table.add(textTable);
        table.add("Type");

        gives = new HorizontalGroup();
        gives.addActor(new Label("ABC", game.skin));
        gives.addActor(new Label("ABC", game.skin));
        table.add(gives);
    }

    public Table getTable() {
        return table;
    }
}
