package com.cardshifter.gdx.ui.cards;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.gdx.CardshifterGame;

import java.util.Map;

/**
 * Created by Simon on 2/8/2015.
 */
public class CardViewHidden implements CardView {
    private final Table table;

    public CardViewHidden(CardshifterClientContext context, int id) {
        this.table = new Table(context.getSkin());
        this.table.add("???").expand().fill();
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
}
