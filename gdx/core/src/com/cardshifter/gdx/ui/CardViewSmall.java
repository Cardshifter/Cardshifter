package com.cardshifter.gdx.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.EntityView;
import com.cardshifter.gdx.ui.res.ResourceView;
import com.cardshifter.gdx.ui.res.ResViewFactory;

import java.util.Map;

public class CardViewSmall implements EntityView {

    private final Table table;
    private final Label effect;
    private final Label name;
    private final ResourceView cost;
    private final Map<String, Object> properties;

    public CardViewSmall(CardshifterGame game, CardInfoMessage cardInfo) {
        this.properties = cardInfo.getProperties();
        table = new Table(game.skin);
        Gdx.app.log("CardView", "Creating for " + cardInfo.getProperties());
        table.defaults().expand();
        name = label(game, cardInfo, "name");
        table.add(name).colspan(2).width(100).row();
        // table.add(image);
        effect = label(game, cardInfo, "effect");
        table.add(effect).colspan(2).row();
        ResViewFactory rvf = new ResViewFactory(game.skin);
        cost = rvf.forFormat(rvf.res("MANA_COST"), rvf.res("SCRAP_COST"));
        table.add(cost.getActor()).colspan(2).right().row();

        table.add(label(game, cardInfo, "creatureType")).left();
        table.add("Stats").right();
        table.setDebug(true, true);

        cost.update(properties);
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
