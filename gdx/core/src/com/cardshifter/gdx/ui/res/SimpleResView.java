package com.cardshifter.gdx.ui.res;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.Map;

/**
 * Created by Simon on 2/8/2015.
 */
public class SimpleResView extends ResView {

    private final String start;
    private final Label label;
    private final String key;

    public SimpleResView(Skin skin, String c, String key) {
        this.start = c;
        this.label = new Label(c, skin);
        this.key = key;
    }

    @Override
    public Actor getActor() {
        return label;
    }

    @Override
    public void update(Map<String, ? extends Object> properties) {
        Object value = properties.get(key);
        label.setText(value == null ? "" : start + value);
    }

}
