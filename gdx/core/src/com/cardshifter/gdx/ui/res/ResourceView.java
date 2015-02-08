package com.cardshifter.gdx.ui.res;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResourceView {

    private final HorizontalGroup actor;
    private final Skin skin;
    private final List<ResView> views;

    public ResourceView(Skin skin, List<ResView> views) {
        this.actor = new HorizontalGroup();
        this.skin = skin;
        this.views = new ArrayList<ResView>(views);
        for (ResView view : views) {
            this.actor.addActor(view.getActor());
        }
    }

    public void update(final Map<String, Object> properties) {
        for (ResView view : views) {
            view.update(properties);
        }
    }

    public Actor getActor() {
        return actor;
    }

}
