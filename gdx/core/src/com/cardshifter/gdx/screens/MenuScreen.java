package com.cardshifter.gdx.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cardshifter.gdx.CardshifterGame;

/**
 * Created by Zomis on 2014-11-11.
 */
public class MenuScreen implements Screen {

    private final Table table;
    private final CardshifterGame game;
    private final String[] availableServers = { "stats.zomis.net:4242", "dwarftowers.com:4242", "127.0.0.1:4242"};
    private final String[] availableWSServers = { "stats.zomis.net:4243", "dwarftowers.com:4243", "127.0.0.1:4243"};

    public MenuScreen(final CardshifterGame game) {
        final Preferences prefs = Gdx.app.getPreferences("cardshifter");
        this.table = new Table();
        this.game = game;
        this.table.setFillParent(true);

        Table inner = new Table();

        final TextField username = new TextField("", game.skin);
        String[] servers = isGWT() ? availableWSServers : availableServers;
        inner.add(username).expand().fill().colspan(servers.length).row();
        username.setText(prefs.getString("username", "YourUserName"));

        HorizontalGroup serverView = new HorizontalGroup();
        for (final String server : servers) {
            final String[] serverData = server.split(":");
            TextButton button = new TextButton(serverData[0], game.skin);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    prefs.putString("username", username.getText());
                    prefs.flush();
                    String hostname = isGWT() ? "ws://" + serverData[0] : serverData[0];
                    game.setScreen(new ClientScreen(game, hostname, Integer.parseInt(serverData[1]), username.getText()));
                }
            });
            serverView.addActor(button);
        }
        inner.add(serverView).expand().fill();
        table.add(inner);
    }

    private boolean isGWT() {
        return Gdx.app.getType() == Application.ApplicationType.WebGL;
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {
        game.stage.addActor(table);
    }

    @Override
    public void hide() {
        table.remove();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
