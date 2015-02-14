package com.cardshifter.gdx.ui.zones;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.IntSet;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.ZoneMessage;
import com.cardshifter.gdx.ui.cards.CardView;

public abstract class ZoneView {

    protected final IntSet entities = new IntSet();
    private final int id;
    private final int owner;
    private final String name;

    public ZoneView(ZoneMessage message) {
        this.id = message.getId();
        this.entities.addAll(message.getEntities());
        this.owner = message.getOwner();
        this.name = message.getName();
    }

    public final CardView addCard(CardInfoMessage message) {
        this.entities.add(message.getId());
        return onAddCard(message);
    }

    protected abstract CardView onAddCard(CardInfoMessage message);
    protected void onRemoveCard(int id) { }
    public abstract Actor getActor();

    public final void removeCard(int id) {
        this.entities.remove(id);
        this.onRemoveCard(id);
    }

    public boolean hasCard(int id) {
        return entities.contains(id);
    }

}
