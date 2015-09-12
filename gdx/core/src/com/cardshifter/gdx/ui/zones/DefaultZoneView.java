package com.cardshifter.gdx.ui.zones;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
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
    private float initialCardViewWidth = 0;
    private float initialCardViewHeight = 0;
    private boolean cardZoomedIn = false;

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
            view = new CardViewSmall(context, message, this, false);
        }
        group.addActor(view.getActor());
        return view;
    }

    @Override
    public Actor getActor() {
        return group;
    }

    /*
	@Override
	public void zoomCard(CardViewSmall cardView) {
		this.gameScreen.zoomCard(cardView);
		System.out.println("Zoom card found in DefaultZoneView");
	}

	@Override
	public void endZoom(CardViewSmall cardView) {
		System.out.println("DefaultZoneView ending zoom");
	}
	*/
    
	@Override
	public void zoomCard(final CardViewSmall cardView) {
		if (this.cardZoomedIn) {
			return;
		}
		final CardViewSmall cardViewCopy = new CardViewSmall(this.context, cardView.cardInfo, this, true);
		cardViewCopy.getActor().setPosition(Gdx.graphics.getWidth()/2.7f, Gdx.graphics.getHeight()/30);
		this.context.getStage().addActor(cardViewCopy.getActor());
		this.initialCardViewWidth = cardView.getActor().getWidth();
		this.initialCardViewHeight = cardView.getActor().getHeight();
		SequenceAction sequence = new SequenceAction();
		Runnable adjustForZoom = new Runnable() {
		    @Override
		    public void run() {
		    	cardViewCopy.zoom();
		    }
		};
		sequence.addAction(Actions.sizeTo(Gdx.graphics.getWidth()/4, Gdx.graphics.getHeight()*0.9f, 0.2f));
		sequence.addAction(Actions.run(adjustForZoom));		
		cardViewCopy.getActor().addAction(sequence);
		this.cardZoomedIn = true;
	}
	
	@Override
	public void endZoom(final CardViewSmall cardView) {
		if (cardView.isZoomed){
    		SequenceAction sequence = new SequenceAction();
    		Runnable endZoom = new Runnable() {
    		    @Override
    		    public void run() {
    		    	cardView.endZoom();
    		    	cardView.getActor().remove();
    		    	DefaultZoneView.this.cardZoomedIn = false;
    		    }
    		};
    		sequence.addAction(Actions.sizeTo(this.initialCardViewWidth, this.initialCardViewHeight, 0.2f));
    		sequence.addAction(Actions.run(endZoom));
    		cardView.getActor().addAction(sequence);
		}
	}
}
