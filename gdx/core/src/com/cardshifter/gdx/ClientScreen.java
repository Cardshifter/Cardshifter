package com.cardshifter.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.cardshifter.gdx.api.incoming.LoginMessage;
import com.cardshifter.api.messages.Message;

import java.io.IOException;

/**
 * Created by Zomis on 2014-11-11.
 */
public class ClientScreen implements Screen, CardshifterMessageHandler {
    private final CardshifterClient client;

    public ClientScreen(CardshifterGame game, String host, int port) {
        client = new CardshifterClient(host, port, this);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                client.send(new LoginMessage("Zomis_GDX"));
            }
        });
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

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

    @Override
    public void handle(Message message) {
        Gdx.app.log("Client", "Received " + message);
    }
}
