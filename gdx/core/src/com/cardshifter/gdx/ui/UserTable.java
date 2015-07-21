package com.cardshifter.gdx.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class UserTable {

    private final Table table;
    private final int id;
    private final String name;
    private final Label nameLabel;

    public UserTable(Skin skin, int userId, String userName) {
        this.id = userId;
        this.name = userName;
        this.table = new Table(skin);
        this.nameLabel = new Label(this.name, skin);
        this.table.add(nameLabel);
    }
    
    public void markSelected() {
    	this.nameLabel.setColor(Color.CYAN);
    }
    
    public void deselect() {
    	this.nameLabel.setColor(Color.WHITE);
    }

    public Table getTable() {
        return table;
    }

    public void remove() {
        table.remove();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "UserTable " + id + " " + name;
    }
}
