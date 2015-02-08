package com.cardshifter.gdx.ui.res;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.Arrays;
import java.util.Map;

public class ResViewFactory {

    private final Skin skin;

    public ResViewFactory(Skin skin) {
        this.skin = skin;
    }

    public ResView res(String key) {
        return new SimpleResView(skin, key.substring(0, 1), key);
    }

    public ResView coloredRes(String key, Map<String, Object> properties) {
        return new ColoredResView(skin, key, properties);
    }

    public ResView coloredRes(String key, String originalKey) {
        return new ColoredResView(skin, key, originalKey);
    }

    public ResView str(String s) {
        final Label label = new Label(s, skin);
        return new ResView() {
            @Override
            public Actor getActor() {
                return label;
            }

            @Override
            public void update(Map<String, Object> properties) {

            }
        };
    }

    public ResourceView forFormat(ResView... format) {
        return new ResourceView(skin, Arrays.asList(format));
    }

}
