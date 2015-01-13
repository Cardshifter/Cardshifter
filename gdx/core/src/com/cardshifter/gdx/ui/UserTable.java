package com.cardshifter.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class UserTable {

    private final Table table;
    private final int id;
    private final String name;

    public UserTable(Skin skin, int userId, String userName) {
        this.id = userId;
        this.name = userName;
        this.table = new Table(skin);
        this.table.add(userName);
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
