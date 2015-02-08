package com.cardshifter.gdx.ui.zones;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.IntSet;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.ZoneMessage;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.ui.zones.ZoneView;

public class CompactHiddenZoneView extends ZoneView {

    private final Label label;
    private final IntSet entities = new IntSet();
    private final ZoneMessage message;

    public CompactHiddenZoneView(CardshifterGame game, ZoneMessage message) {
        entities.addAll(message.getEntities());
        this.label = new Label(String.valueOf(entities.size), game.skin);
        this.message = message;
    }

    @Override
    public Actor getActor() {
        return label;
    }

    @Override
    public void addCard(CardInfoMessage message) {
        entities.add(message.getId());
        update();
    }

    private void update() {
        this.label.setText(message.getOwner() + ": " + message.getName() + ": " + String.valueOf(entities.size));
    }

    @Override
    public void removeCard(int id) {
        entities.remove(id);
        update();
    }
}
