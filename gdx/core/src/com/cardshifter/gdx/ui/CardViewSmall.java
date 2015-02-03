package com.cardshifter.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.gdx.CardshifterGame;

public class CardViewSmall {

    private final Table table;

    public CardViewSmall(CardshifterGame game, CardInfoMessage cardInfo) {
        table = new Table(game.skin);
        Label name = new Label((String) cardInfo.getProperties().get("name"), game.skin);
        name.setEllipse(true);
        table.add(name).colspan(2).maxWidth(110).expandX().fillX().row();
        // table.add(image);
        table.add("Effect").colspan(2).row();
        table.add("Cost").colspan(2).right().row();
        table.add("Type").left();
        table.add("Stats").right();
    }

    public Table getTable() {
        return table;
    }
}
