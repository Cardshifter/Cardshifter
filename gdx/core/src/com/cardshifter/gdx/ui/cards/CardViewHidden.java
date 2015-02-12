package com.cardshifter.gdx.ui.cards;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.api.outgoing.ZoneChangeMessage;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;
import com.cardshifter.gdx.ui.CardshifterClientContext;
import com.cardshifter.gdx.ui.zones.ZoneView;

import java.util.Map;

/**
 * Created by Simon on 2/8/2015.
 */
public class CardViewHidden extends DefaultCardView {
    private final Table table;
    private final int id;
    private final Texture bg = new Texture(Gdx.files.internal("bg.png"));

    public CardViewHidden(CardshifterClientContext context, int id) {
        this.table = new Table(context.getSkin());
        Image img = new Image(bg);
        this.table.add(img).prefSize(60, 80);
        this.id = id;
        Gdx.app.log("CardView", "creating hidden " + id);
    }

    @Override
    public Map<String, Object> getInfo() {
        return null;
    }

    @Override
    public Actor getActor() {
        return table;
    }

    @Override
    public void set(Object key, Object value) {

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
