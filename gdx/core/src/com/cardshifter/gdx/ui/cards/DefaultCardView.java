package com.cardshifter.gdx.ui.cards;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.api.outgoing.ZoneChangeMessage;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;
import com.cardshifter.gdx.ui.zones.ZoneView;

/**
 * Created by Simon on 2/12/2015.
 */
public abstract class DefaultCardView implements CardView {

    @Override
    public void zoneMove(ZoneChangeMessage message, ZoneView destinationZone, CardView newCardView) {
        Gdx.app.log("CardView", "Zonechange: " + message + " to " + destinationZone);
        if (destinationZone == null) {
            entityRemoved();
            return;
        }

        final Actor next = newCardView.getActor();
        next.addAction(Actions.alpha(0.7f, 0.3f));
        Gdx.app.log("test", "next is " + next);
        getActor().addAction(Actions.delay(0.3f, Actions.run(new Runnable() {
            @Override
            public void run() {
                Actor actor = next;
                while (actor != null) {
                    Gdx.app.log("test", "actor " + actor + " pos " + actor.getX() + ", " + actor.getY());
                    actor = actor.getParent();
                }
                Gdx.app.log("test", "next is " + next);
                Vector2 destination = new Vector2(0, 0);
                Gdx.app.log("CardView", "Position1 " + next.getX() + ", " + next.getY());
                destination = next.localToStageCoordinates(destination);
                Gdx.app.log("CardView", "Position2 " + destination.x + ", " + destination.y);
                //    next.addAction(Actions.hide());
                getActor().addAction(Actions.sequence(Actions.moveTo(destination.x, destination.y, 2.0f), Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        final float delay = 1.5f;
                        getActor().addAction(Actions.sequence(Actions.fadeOut(delay), removeThis()));
                        next.addAction(Actions.sequence(Actions.show(), Actions.fadeIn(delay)));
                    }
                })));
            }
        })));
    }

    @Override
    public void remove() {
        getActor().remove();
    }

    @Override
    public void setTargetable(TargetStatus targetable, TargetableCallback callback) {

    }

    @Override
    public void usableAction(UsableActionMessage message) {

    }

    @Override
    public void clearUsableActions() {

    }

    @Override
    public void entityRemoved() {
        getActor().addAction(Actions.sequence(Actions.fadeOut(1f), removeThis()));
    }

    private Action removeThis() {
        return Actions.run(new Runnable() {
            @Override
            public void run() {
                getActor().remove();
            }
        });
    }

}
