package com.cardshifter.gdx.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by Simon on 2/11/2015.
 */
public class DeckCardView extends Table {

    private final Label count;
    private final int id;
    private final String name;
    private final DeckBuilderScreen screen;

    public DeckCardView(Skin skin, int id, String name, DeckBuilderScreen screen) {
        super(skin);
        this.count = new Label("", skin);
        this.add(count).left().expand().fill();
        this.add(new Label(name, skin)).right();
        this.name = name;
        this.id = id;
        this.screen = screen;
        setName(name);
        
        this.setTouchable(Touchable.enabled);
        this.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                DeckCardView.this.screen.removeCardFromDeck(DeckCardView.this.id);
            }
        });
    }

    public void setCount(int count) {
        if (count == 0) {
            this.remove();
            return;
        }
        this.count.setText(String.valueOf(count));
    }
    
    public int getCount() {
    	String countString = this.count.getText().toString();
    	if (countString != null && !countString.equals("")) {
    		return  Integer.parseInt(this.count.getText().toString());
    	} else {
    		return 0;
    	}
    }

    public int getId() {
        return id;
    }
    
    public void clicked() {
    	System.out.println("DeckvView: clicked on " + String.valueOf(this.id));
    }

}
