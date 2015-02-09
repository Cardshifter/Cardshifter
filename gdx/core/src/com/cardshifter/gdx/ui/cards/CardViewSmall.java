package com.cardshifter.gdx.ui.cards;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;
import com.cardshifter.gdx.ui.CardshifterClientContext;
import com.cardshifter.gdx.ui.EntityView;
import com.cardshifter.gdx.ui.res.ResourceView;
import com.cardshifter.gdx.ui.res.ResViewFactory;

import java.util.HashMap;
import java.util.Map;

public class CardViewSmall implements CardView {

    private final Table table;
    private final Label effect;
    private final Label name;
    private final ResourceView cost;
    private final ResourceView stats;
    private final Map<String, Object> properties;
    private final int id;

    public CardViewSmall(CardshifterClientContext context, CardInfoMessage cardInfo) {
        this.properties = new HashMap<String, Object>(cardInfo.getProperties());
        this.id = cardInfo.getId();
        table = new Table(context.getSkin());
        Gdx.app.log("CardView", "Creating for " + cardInfo.getProperties());
        table.defaults().expand();
        name = label(context, cardInfo, "name");
        table.add(name).colspan(2).width(100).row();
        // table.add(image);
        effect = label(context, cardInfo, "effect");
        table.add(effect).colspan(2).width(100).row();
        ResViewFactory rvf = new ResViewFactory(context.getSkin());
        cost = rvf.forFormat(rvf.res("MANA_COST"), rvf.res("SCRAP_COST"));
        table.add(cost.getActor()).colspan(2).right().row();

        stats = rvf.forFormat(rvf.coloredRes("ATTACK", properties), rvf.str("/"), rvf.coloredRes("HEALTH", "MAX_HEALTH"));
        table.add(label(context, cardInfo, "creatureType")).left();
        table.add(stats.getActor()).right();
        table.setDebug(true, true);

        cost.update(properties);
        stats.update(properties);

        table.setTouchable(Touchable.enabled);
        table.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CardViewSmall.this.clicked();
            }
        });
    }

    private void clicked() {
        Gdx.app.log("CardView", "clicked on " + id);

    }

    public static Label label(CardshifterClientContext context, CardInfoMessage message, String key) {
        Object value = message.getProperties().get(key);
        Label label = new Label(String.valueOf(value == null ? "" : value), context.getSkin());
        label.setEllipse(true);
        return label;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public void set(Object key, Object value) {
        properties.put((String) key, value);
        cost.update(properties);
        stats.update(properties);
    }

    @Override
    public void remove() {
        table.remove();
    }

    @Override
    public void setTargetable(TargetStatus targetable, TargetableCallback callback) {
        if (targetable == TargetStatus.TARGETABLE) {
            table.setColor(0, 0, 1, 1);
        }
        else if (targetable == TargetStatus.TARGETED) {
            table.setColor(0, 1, 0, 1);
        }
        else {
            table.setColor(1, 1, 1, 1);
        }
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void usableAction(UsableActionMessage message) {

    }

    @Override
    public void clearUsableActions() {

    }

    @Override
    public Map<String, Object> getInfo() {
        return new HashMap<String, Object>(this.properties);
    }
}
