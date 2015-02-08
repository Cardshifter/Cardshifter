package com.cardshifter.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.*;
import com.cardshifter.gdx.ui.ZoneView;

import java.util.HashMap;
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
    private final Map<Integer, ZoneView> zoneViews = new HashMap<Integer, ZoneView>();
    private final Map<Integer, EntityView> entityViews = new HashMap<Integer, EntityView>();
    private final Map<String, Container<Table>> holders = new HashMap<String, Container<Table>>();

    public GameScreen(CardshifterGame game, CardshifterClient client, NewGameMessage message) {
        this.game = game;
        this.client = client;
        this.playerIndex = message.getPlayerIndex();
        this.gameId = message.getGameId();

        this.table = new Table(game.skin);

        Table leftTable = new Table(game.skin);
        Table topTable = new Table(game.skin);
        Table rightTable = new Table(game.skin);
        Table centerTable = new Table(game.skin);

        leftTable.add("players");
        rightTable.add("controls");
        topTable.add(leftTable).left().width(150).expandY().fillY();
        topTable.add(centerTable).center().expandX().expandY().fill();
        topTable.add(rightTable).right().width(150).expandY().fillY();

        addZoneHolder(centerTable, 1 - this.playerIndex, "Hand").top();
        addZoneHolder(centerTable, 1 - this.playerIndex, "Battlefield");
        addZoneHolder(centerTable, this.playerIndex, "Battlefield").bottom();

        this.table.add(topTable).expandY().fill().row();
        addZoneHolder(this.table, this.playerIndex, "Hand");

        this.table.setFillParent(true);
        this.table.setDebug(true, true);
    }

    private Cell<Container<Table>> addZoneHolder(Table table, int i, String name) {
        Container<Table> container = new Container<Table>();
        Cell<Container<Table>> cell = table.add(container).expandX().fillX();
        table.row();
        holders.put(i + name, container);
        return cell;
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
        handlers.put(EntityRemoveMessage.class, new SpecificHandler<EntityRemoveMessage>() {
            @Override
            public void handle(EntityRemoveMessage message) {
                EntityView view = entityViews.get(message.getEntity());
                if (view != null) {
                    view.remove();
                }
            }
        });
        handlers.put(GameOverMessage.class, null);
        handlers.put(PlayerMessage.class, new SpecificHandler<PlayerMessage>() {
            @Override
            public void handle(PlayerMessage message) {
                PlayerView playerView = new PlayerView(game, message);
                entityViews.put(message.getId(), playerView);
            }
        });
        handlers.put(ResetAvailableActionsMessage.class, null);
        handlers.put(UpdateMessage.class, null);
        handlers.put(ZoneChangeMessage.class, null);
        handlers.put(ZoneMessage.class, new SpecificHandler<ZoneMessage>() {
            @Override
            public void handle(ZoneMessage message) {
                Gdx.app.log("GameScreen", "Zone " + message);
                ZoneView zoneView = createZoneView(message);
                if (zoneView != null) {
                    PlayerView view = (PlayerView) entityViews.get(message.getOwner());
                    if (view == null) {
                        Gdx.app.log("GameScreen", "no playerView for " + message.getOwner());
                        return;
                    }
                    String key = view.getIndex() + message.getName();
                    Container<Table> container = holders.get(key);
                    if (container == null) {
                        Gdx.app.log("GameScreen", "no container for " + key);
                        return;
                    }
                    Gdx.app.log("GameScreen", "putting zoneview for " + key);
                    container.setActor((Table) zoneView.getActor());
                    zoneViews.put(message.getId(), zoneView);
                    zoneView.apply(message);
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
