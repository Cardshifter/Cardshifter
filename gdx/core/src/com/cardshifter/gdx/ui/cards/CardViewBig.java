package com.cardshifter.gdx.ui.cards;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.api.outgoing.ZoneChangeMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;
import com.cardshifter.gdx.ui.zones.ZoneView;

import java.util.HashMap;
import java.util.Map;

public class CardViewBig implements CardView {

    private final Table table;
    private final HorizontalGroup costs;
    private final HorizontalGroup gives;
    private final HashMap<String, Object> properties;
    private final int id;

    public CardViewBig(CardshifterGame game, CardInfoMessage cardInfo) {
        this.properties = new HashMap<String, Object>(cardInfo.getProperties());
        this.id = cardInfo.getId();
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

    @Override
    public void entityRemoved() {

    }

    @Override
    public Map<String, Object> getInfo() {
        return new HashMap<String, Object>(this.properties);
    }

    @Override
    public Actor getActor() {
        return table;
    }

    @Override
    public void zoneMove(ZoneChangeMessage message, ZoneView destinationZone, CardView newCardView) {

    }
}
