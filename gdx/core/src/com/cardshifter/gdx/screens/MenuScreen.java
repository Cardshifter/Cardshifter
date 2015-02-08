package com.cardshifter.gdx.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.screens.ClientScreen;

/**
 * Created by Zomis on 2014-11-11.
 */
public class MenuScreen implements Screen {

    private final Table table;
    private final CardshifterGame game;

    public MenuScreen(final CardshifterGame game) {
        this.table = new Table();
        this.game = game;

        TextButton button = new TextButton("Connect to Server", game.skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ClientScreen(game, "127.0.0.1", 4242));
            }
        });
        table.setFillParent(true);
        table.add(button).expand().fill();
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
