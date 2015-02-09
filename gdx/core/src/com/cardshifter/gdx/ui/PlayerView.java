package com.cardshifter.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.PlayerMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;
import com.cardshifter.gdx.ui.res.ResViewFactory;
import com.cardshifter.gdx.ui.res.ResourceView;

import java.util.HashMap;

public class PlayerView implements EntityView {

    private final int id;
    private final int index;
    private final Table table;
    private final ResourceView resources;
    private final HashMap<String, Integer> properties;
    private final HorizontalGroup actions = new HorizontalGroup();
    private final CardshifterClientContext context;

    public PlayerView(CardshifterClientContext context, PlayerMessage message) {
        this.context = context;
        this.table = new Table(context.getSkin());
        this.id = message.getId();
        this.index = message.getIndex();
        ResViewFactory rvf = new ResViewFactory(context.getSkin());
        this.resources = rvf.forFormat(rvf.res("SCRAP"), rvf.str(" "), rvf.res("MANA"), rvf.str(" "), rvf.coloredRes("HEALTH", "MAX_HEALTH"));
        this.properties = new HashMap<String, Integer>(message.getProperties());
        this.table.add(message.getName()).row();
        this.table.add(this.resources.getActor()).row();
        this.table.add(actions);
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

    @Override
    public void setTargetable(TargetStatus targetable, TargetableCallback callback) {

    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void usableAction(UsableActionMessage message) {
        ActionButton button = new ActionButton(context, message);
        actions.addActor(button.getButton());
    }

    @Override
    public void clearUsableActions() {
        actions.clearChildren();
    }
}
