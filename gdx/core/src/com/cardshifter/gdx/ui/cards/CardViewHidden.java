package com.cardshifter.gdx.ui.cards;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;
import com.cardshifter.gdx.ui.CardshifterClientContext;

import java.util.Map;

/**
 * Created by Simon on 2/8/2015.
 */
public class CardViewHidden implements CardView {
    private final Table table;
    private final int id;

    public CardViewHidden(CardshifterClientContext context, int id) {
        this.table = new Table(context.getSkin());
        this.table.add("???").expand().fill();
        this.id = id;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public Map<String, Object> getInfo() {
        return null;
    }

    @Override
    public void set(Object key, Object value) {

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

    }

    @Override
    public void clearUsableActions() {

    }
}
