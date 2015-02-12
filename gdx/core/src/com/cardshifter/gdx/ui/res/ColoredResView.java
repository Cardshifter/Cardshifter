package com.cardshifter.gdx.ui.res;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.Map;

/**
 * Created by Simon on 2/8/2015.
 */
public class ColoredResView extends ResView {

    private final Label label;
    private final String key;
    private final String originalKey;
    private int original;

    public ColoredResView(Skin skin, String key, Map<String, Object> properties) {
        this(skin, key, (String) null);
        Integer value = (Integer) properties.get(key);
        this.original = value == null ? 0 : value;
    }

    public ColoredResView(Skin skin, String key, String originalKey) {
        this.label = new Label("", skin);
        this.key = key;
        this.originalKey = originalKey;
    }

    @Override
    public Actor getActor() {
        return label;
    }

    @Override
    public void update(Map<String, ? extends Object> properties) {
        if (originalKey != null) {
            Integer i = (Integer) properties.get(originalKey);
            if (i != null) {
                original = i;
            }
        }
        Integer value = (Integer) properties.get(key);
        if (value == null) {
            return;
        }
        this.label.setText(String.valueOf(value));
        if (value < original) {
            this.label.setColor(1, 0, 0, 1);
        }
        else if (value > original) {
            this.label.setColor(0, 1, 0, 1);
        }
        else {
            this.label.setColor(1, 1, 1, 1);
        }
    }
}
