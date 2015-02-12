package com.cardshifter.gdx.ui.res;

import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.Map;

public abstract class ResView {

    public abstract Actor getActor();
    public abstract void update(Map<String, ? extends Object> properties);

}
