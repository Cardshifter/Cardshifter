package com.cardshifter.gdx;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by Simon on 2/8/2015.
 */
public enum TargetStatus {

    NOT_TARGETABLE(new Color(1, 1, 1, 1)),
    TARGETABLE(new Color(0, 0, 1, 1)),
    TARGETED(new Color(0, 1, 0, 1)),
    ;

    private final Color color;

    private TargetStatus(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
