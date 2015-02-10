package com.cardshifter.gdx.screens;

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
    private final String[] availableServers = { "stats.zomis.net:4242", "dwarftowers.com:4242" };

    public MenuScreen(final CardshifterGame game) {
        final Preferences prefs = Gdx.app.getPreferences("cardshifter");
        this.table = new Table();
        this.game = game;
        this.table.setFillParent(true);

        Table inner = new Table();

        final TextField username = new TextField("", game.skin);
        inner.add(username).expand().fill().colspan(availableServers.length).row();
        username.setText(prefs.getString("username", "YourUserName"));

        HorizontalGroup servers = new HorizontalGroup();
        for (final String server : availableServers) {
            final String[] serverData = server.split(":");
            TextButton button = new TextButton(serverData[0], game.skin);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    prefs.putString("username", username.getText());
                    prefs.flush();
                    game.setScreen(new ClientScreen(game, serverData[0], Integer.parseInt(serverData[1]), username.getText()));
                }
            });
            servers.addActor(button);
        }
        inner.add(servers).expand().fill();
        table.add(inner);
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
