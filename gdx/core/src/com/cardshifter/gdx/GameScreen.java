package com.cardshifter.gdx;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.*;
import com.cardshifter.gdx.ui.ZoneView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Simon on 1/31/2015.
 */
public class GameScreen implements Screen {

    private final CardshifterGame game;
    private final CardshifterClient client;
    private final int playerIndex;
    private final int gameId;

    private final Table table;
    private final List<PlayerView> playerViews = new ArrayList<PlayerView>();
    private final Map<Integer, ZoneView> zoneViews = new HashMap<Integer, ZoneView>();

    public GameScreen(CardshifterGame game, CardshifterClient client, NewGameMessage message) {
        this.game = game;
        this.client = client;
        this.playerIndex = message.getPlayerIndex();
        this.gameId = message.getGameId();

        this.table = new Table(game.skin);

        this.playerViews.add(new PlayerView(game));
        this.playerViews.add(new PlayerView(game));

        this.table.add(playerViews.get(0).getActor()).row();
        this.table.add(playerViews.get(1).getActor()).row();
        this.table.setDebug(true, true);
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

    public Map<Class<? extends Message>, SpecificHandler<?>> getHandlers() {
        Map<Class<? extends Message>, SpecificHandler<?>> handlers =
                new HashMap<Class<? extends Message>, SpecificHandler<?>>();

        handlers.put(AvailableTargetsMessage.class, null);
        handlers.put(CardInfoMessage.class, new SpecificHandler<CardInfoMessage>() {
            @Override
            public void handle(CardInfoMessage message) {
                ZoneView zone = getZoneView(message.getZone());
                if (zone != null) {
                    zone.addCard(message);
                }
            }
        });
        handlers.put(EntityRemoveMessage.class, null);
        handlers.put(GameOverMessage.class, null);
        handlers.put(PlayerMessage.class, new SpecificHandler<PlayerMessage>() {
            @Override
            public void handle(PlayerMessage message) {
                while (playerViews.size() < message.getIndex()) {
                    playerViews.add(new PlayerView(game));
                }
                playerViews.get(message.getIndex()).set(message);
            }
        });
        handlers.put(ResetAvailableActionsMessage.class, null);
        handlers.put(UpdateMessage.class, null);
        handlers.put(ZoneChangeMessage.class, null);
        handlers.put(ZoneMessage.class, new SpecificHandler<ZoneMessage>() {
            @Override
            public void handle(ZoneMessage message) {
                ZoneView zoneView = createZoneView(message);
                zoneViews.put(message.getId(), zoneView);
                if (zoneView != null) {
                    zoneView.apply(message);
                    table.add(zoneView.getActor()).row();
                    table.setDebug(true, true);
                }
            }
        });

        return handlers;
    }

    private ZoneView createZoneView(ZoneMessage message) {
        String type = message.getName();
        if (type.equals("Battlefield")) {
            return new DefaultZoneView(game, message);
        }
        if (type.equals("Hand")) {
            return new DefaultZoneView(game, message);
        }
        if (type.equals("Deck")) {
            return new CompactHiddenZoneView(game, message);
        }
        if (type.equals("Cards")) {
            return null; // Card models only
        }
        throw new RuntimeException("Unknown ZoneView type: " + message.getName());
    }

    private ZoneView getZoneView(int id) {
        return this.zoneViews.get(id);
    }
}
