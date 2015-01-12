package com.cardshifter.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.AvailableModsMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zomis on 2014-11-11.
 */
public class ClientScreen implements Screen, CardshifterMessageHandler {
    private final CardshifterClient client;
    private final Map<Class<? extends Message>, SpecificHandler<?>> handlerMap = new HashMap<Class<? extends Message>, SpecificHandler<?>>();
    private final Table table;
    private final HorizontalGroup mods;
    private final CardshifterGame game;

    public ClientScreen(final CardshifterGame game, String host, int port) {
        this.game = game;
        client = new CardshifterClient(host, port, this);
        table = new Table(game.skin);
        table.setFillParent(true);
        mods = new HorizontalGroup();
        table.add(mods).bottom().expand().fill();
        table.setDebug(true, true);
        handlerMap.put(AvailableModsMessage.class, new SpecificHandler<AvailableModsMessage>() {
            @Override
            public void handle(AvailableModsMessage message) {
                Gdx.app.log("handle", "handle " + message);
                for (String mod : message.getMods()) {
                    mods.addActor(new TextButton(mod, game.skin));
                }
            }
        });
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
