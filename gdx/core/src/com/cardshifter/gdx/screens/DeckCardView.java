package com.cardshifter.gdx.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Created by Simon on 2/11/2015.
 */
public class DeckCardView extends Table {

    private final Label count;
    private final int id;
    private final String name;

    public DeckCardView(Skin skin, int id, String name) {
        super(skin);
        this.count = new Label("", skin);
        this.add(count).left().expand().fill();
        this.add(new Label(name, skin)).right();
        this.name = name;
        this.id = id;
        setName(name);
    }

    public void setCount(int count) {
        if (count == 0) {
            this.remove();
            return;
        }
        this.count.setText(String.valueOf(count));
    }

    public int getId() {
        return id;
    }

}
