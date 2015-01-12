package com.cardshifter.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.messages.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zomis on 2014-11-11.
 */
public class ClientScreen implements Screen, CardshifterMessageHandler {
    private final CardshifterClient client;
    private final Map<Class<? extends Message>, SpecificHandler<?>> handlerMap = new HashMap<Class<? extends Message>, SpecificHandler<?>>();
    private final CardshifterGame game;

    public ClientScreen(final CardshifterGame game, String host, int port) {
        this.game = game;
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
    public void handle(final Message message) {
        Gdx.app.log("Client", "Received " + message);
        final SpecificHandler<Message> handler = (SpecificHandler<Message>) handlerMap.get(message.getClass());
        if (handler != null) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    handler.handle(message);
                }
            });
        }
    }
}
