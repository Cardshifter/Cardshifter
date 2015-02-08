package com.cardshifter.gdx.ui.cards;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.ui.EntityView;
import com.cardshifter.gdx.ui.res.ResourceView;
import com.cardshifter.gdx.ui.res.ResViewFactory;

import java.util.Map;

public class CardViewSmall implements EntityView {

    private final Table table;
    private final Label effect;
    private final Label name;
    private final ResourceView cost;
    private final ResourceView stats;
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
        table.add(effect).colspan(2).width(100).row();
        ResViewFactory rvf = new ResViewFactory(game.skin);
        cost = rvf.forFormat(rvf.res("MANA_COST"), rvf.res("SCRAP_COST"));
        table.add(cost.getActor()).colspan(2).right().row();

        stats = rvf.forFormat(rvf.coloredRes("ATTACK", properties), rvf.str("/"), rvf.coloredRes("HEALTH", "MAX_HEALTH"));
        table.add(label(game, cardInfo, "creatureType")).left();
        table.add(stats.getActor()).right();
        table.setDebug(true, true);

        cost.update(properties);
        stats.update(properties);
    }

    public static Label label(CardshifterGame game, CardInfoMessage message, String key) {
        Object value = message.getProperties().get(key);
        Label label = new Label(String.valueOf(value == null ? "" : value), game.skin);
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
