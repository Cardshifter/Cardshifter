package com.cardshifter.gdx.ui.zones;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.ZoneMessage;
import com.cardshifter.gdx.ZoomCardCallback;
import com.cardshifter.gdx.ui.CardshifterClientContext;
import com.cardshifter.gdx.ui.EntityView;
import com.cardshifter.gdx.ui.cards.CardView;
import com.cardshifter.gdx.ui.cards.CardViewHidden;
import com.cardshifter.gdx.ui.cards.CardViewSmall;

import java.util.Map;

public class DefaultZoneView extends ZoneView implements ZoomCardCallback {

    private final HorizontalGroup group;
    private final CardshifterClientContext context;

    public DefaultZoneView(CardshifterClientContext context, ZoneMessage message, Map<Integer, EntityView> viewMap) {
        super(message);
        this.group = new HorizontalGroup();
        this.group.space(5);
        this.group.fill();
        this.context = context;
        for (int id : message.getEntities()) {
            viewMap.put(id, addCard(new CardInfoMessage(message.getId(), id, null)));
        }
    }

    @Override
    public final CardView onAddCard(CardInfoMessage message) {
        CardView view;
        if (message.getProperties() == null) {
            view = new CardViewHidden(context, message.getId());
        }
        else {
            view = new CardViewSmall(context, message, this);
        }
        group.addActor(view.getActor());
        return view;
    }

    @Override
    public Actor getActor() {
        return group;
    }

	@Override
	public void zoomCard(CardViewSmall cardView) {
		System.out.println("Zoom card found in DefaultZoneView");
	}
}
