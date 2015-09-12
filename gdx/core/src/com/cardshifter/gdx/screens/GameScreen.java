package com.cardshifter.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.*;
import com.cardshifter.gdx.*;
import com.cardshifter.gdx.ui.CardshifterClientContext;
import com.cardshifter.gdx.ui.EntityView;
import com.cardshifter.gdx.ui.PlayerView;
import com.cardshifter.gdx.ui.cards.CardView;
import com.cardshifter.gdx.ui.cards.CardViewSmall;
import com.cardshifter.gdx.ui.zones.CompactHiddenZoneView;
import com.cardshifter.gdx.ui.zones.DefaultZoneView;
import com.cardshifter.gdx.ui.zones.ZoneView;

import java.util.*;
import java.util.List;

/**
 * Created by Simon on 1/31/2015.
 */
public class GameScreen implements Screen, TargetableCallback {

    private final CardshifterGame game;
    private final CardshifterClient client;
    private final int playerIndex;
    private final int gameId;

    private final Table table;
    private final Map<Integer, ZoneView> zoneViews = new HashMap<Integer, ZoneView>();
    private final Map<Integer, EntityView> entityViews = new HashMap<Integer, EntityView>();
    private final Map<String, Container<Actor>> holders = new HashMap<String, Container<Actor>>();
    private final List<EntityView> targetsSelected = new ArrayList<EntityView>();
    private final Screen parentScreen;
    private AvailableTargetsMessage targetsAvailable;
    private final TargetableCallback onTarget = new TargetableCallback() {
        @Override
        public boolean addEntity(EntityView view) {
            if (targetsSelected.contains(view)) {
                targetsSelected.remove(view);
                Gdx.app.log("GameScreen", "Removing selection " + view.getId());
                view.setTargetable(TargetStatus.TARGETABLE, this);
                return false;
            }

            if (targetsAvailable != null && targetsAvailable.getMax() == 1 && targetsAvailable.getMin() == 1) {
                Gdx.app.log("GameScreen", "Sending selection " + view.getId());
                client.send(new UseAbilityMessage(gameId, targetsAvailable.getEntity(), targetsAvailable.getAction(), new int[]{ view.getId() }));
                return false;
            }

            Gdx.app.log("GameScreen", "Adding selection " + view.getId());
            view.setTargetable(TargetStatus.TARGETED, this);
            return targetsSelected.add(view);
        }
    };
    private final CardshifterClientContext context;
    //private final float screenWidth;
    private final float screenHeight;

    public GameScreen(final CardshifterGame game, final CardshifterClient client, NewGameMessage message, final Screen parentScreen) {
        this.parentScreen = parentScreen;
        this.game = game;
        this.client = client;
        this.playerIndex = message.getPlayerIndex();
        this.gameId = message.getGameId();
        this.context = new CardshifterClientContext(game.skin, message.getGameId(), client, game.stage);
        //this.screenWidth = CardshifterGame.STAGE_WIDTH;
        this.screenHeight = CardshifterGame.STAGE_HEIGHT;

        this.table = new Table(game.skin);

        Table leftTable = new Table(game.skin);
        Table topTable = new Table(game.skin);
        //Table rightTable = new Table(game.skin);
        Table centerTable = new Table(game.skin);

        TextButton backToMenu = new TextButton("Back to menu", game.skin);
        backToMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(parentScreen);
            }
        });
        leftTable.add(backToMenu).expandX().fill().row();
        addZoneHolder(leftTable, 1 - this.playerIndex, "").expandY().fillY();
        addZoneHolder(leftTable, this.playerIndex, "").expandY().fillY();
        leftTable.add("controls").row();
        TextButton actionDone = new TextButton("Done", game.skin);
        actionDone.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (targetsAvailable != null) {
                    int selected = targetsSelected.size();
                    if (selected >= targetsAvailable.getMin() && selected <= targetsAvailable.getMax()) {
                        int[] targets = new int[targetsSelected.size()];
                        for (int i = 0; i < targets.length; i++) {
                            targets[i] = targetsSelected.get(i).getId();
                        }
                        UseAbilityMessage message = new UseAbilityMessage(gameId, targetsAvailable.getEntity(), targetsAvailable.getAction(), targets);
                        client.send(message);
                    }
                }
            }
        });
        leftTable.add(actionDone);
        topTable.add(leftTable).left().expandY().fillY();
        topTable.add(centerTable).center().expandX().expandY().fill();
        //topTable.add(rightTable).right().width(150).expandY().fillY();

        addZoneHolder(centerTable, 1 - this.playerIndex, "Hand").top().height(this.screenHeight/4);
        addZoneHolder(centerTable, 1 - this.playerIndex, "Battlefield").height(this.screenHeight/4);
        addZoneHolder(centerTable, this.playerIndex, "Battlefield").height(this.screenHeight/4);

        this.table.add(topTable).expand().fill().row();
        addZoneHolder(this.table, this.playerIndex, "Hand").height(140).expandX().fill();

        this.table.setFillParent(true);
    }

    private Cell<Container<Actor>> addZoneHolder(Table table, int i, String name) {
        Container<Actor> container = new Container<Actor>();
        container.setName(name);
//        container.fill();
        Cell<Container<Actor>> cell = table.add(container).expandX().fillX();
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

        handlers.put(AvailableTargetsMessage.class, new SpecificHandler<AvailableTargetsMessage>() {
            @Override
            public void handle(AvailableTargetsMessage message) {
                targetsAvailable = message;
                targetsSelected.clear();
                for (EntityView view : entityViews.values()) {
                    view.setTargetable(TargetStatus.NOT_TARGETABLE, onTarget);
                }
                for (int id : message.getTargets()) {
                    EntityView view = entityViews.get(id);
                    if (view != null) {
                        view.setTargetable(TargetStatus.TARGETABLE, onTarget);
                    }
                }
            }
        });
        handlers.put(UsableActionMessage.class, new SpecificHandler<UsableActionMessage>() {
            @Override
            public void handle(UsableActionMessage message) {
                int id = message.getId();
                EntityView view = entityViews.get(id);
                if (view != null) {
                    view.usableAction(message);
                    if (view instanceof CardViewSmall) {
                    	((CardViewSmall)view).setUsable(GameScreen.this);
                    }
                }
                
            }
        });
        handlers.put(CardInfoMessage.class, new SpecificHandler<CardInfoMessage>() {
            @Override
            public void handle(CardInfoMessage message) {
                ZoneView zone = getZoneView(message.getZone());
                if (zone != null) {
                    zone.removeCard(message.getId());
                }
                EntityView entityView = entityViews.remove(message.getId());
                if (entityView != null) {
                    entityView.remove();
                }
                if (zone != null) {
                    entityViews.put(message.getId(), zone.addCard(message));
                }
            }
        });
        handlers.put(EntityRemoveMessage.class, new SpecificHandler<EntityRemoveMessage>() {
            @Override
            public void handle(EntityRemoveMessage message) {
                EntityView view = entityViews.get(message.getEntity());
                for (ZoneView zone : zoneViews.values()) {
                    if (zone.hasCard(message.getEntity())) {
                        zone.removeCard(message.getEntity());
                    }
                }
                if (view != null) {
                    view.entityRemoved();
                    entityViews.remove(message.getEntity());
                }
            }
        });
        handlers.put(GameOverMessage.class, new SpecificHandler<GameOverMessage>() {
            @Override
            public void handle(GameOverMessage message) {
                Dialog dialog = new Dialog("Game Over!", context.getSkin()) {
                    @Override
                    protected void result(Object object) {
                        game.setScreen(parentScreen);
                    }
                };
                dialog.button("OK");
                dialog.show(context.getStage());
            }
        });
        handlers.put(PlayerMessage.class, new SpecificHandler<PlayerMessage>() {
            @Override
            public void handle(PlayerMessage message) {
                PlayerView playerView = new PlayerView(context, message);
                entityViews.put(message.getId(), playerView);

                Container<Actor> holder = holders.get(String.valueOf(message.getIndex()));
                if (holder != null) {
                    holder.setActor(playerView.getActor());
                }
            }
        });
        handlers.put(ResetAvailableActionsMessage.class, new SpecificHandler<ResetAvailableActionsMessage>() {
            @Override
            public void handle(ResetAvailableActionsMessage message) {
                for (EntityView view : entityViews.values()) {
                    view.setTargetable(TargetStatus.NOT_TARGETABLE, null);
                    view.clearUsableActions();
                }
            }
        });
        handlers.put(UpdateMessage.class, new SpecificHandler<UpdateMessage>() {
            @Override
            public void handle(UpdateMessage message) {
                EntityView entityView = entityViews.get(message.getId());
                if (entityView != null) {
                    entityView.set(message.getKey(), message.getValue());
                }
            }
        });
        handlers.put(ZoneChangeMessage.class, new SpecificHandler<ZoneChangeMessage>() {
            @Override
            public void handle(ZoneChangeMessage message) {
                ZoneView oldZone = getZoneView(message.getSourceZone()); // can be null
                ZoneView destinationZone = getZoneView(message.getDestinationZone());
                int id = message.getEntity();
                CardView entityView = (CardView) entityViews.remove(id); // can be null

                if (oldZone != null) {
                    oldZone.removeCard(id);
                }

                if (destinationZone != null) {
                    CardView newCardView = destinationZone.addCard(new CardInfoMessage(message.getDestinationZone(), id,
                            entityView == null ? null : entityView.getInfo()));
                    if (entityView != null) {
                        entityView.zoneMove(message, destinationZone, newCardView);
                    }
                    entityViews.put(id, newCardView);
                }
                else {
                    if (entityView != null) {
                        entityView.zoneMove(message, destinationZone, null);
                    }
                }
/*
Send to AI Medium: ZoneChangeMessage [entity=95, sourceZone=72, destinationZone=73]
Send to AI Medium: CardInfo: 95 in zone 73 - {SCRAP=1, TAUNT=1, MAX_HEALTH=1, SICKNESS=1, MANA_COST=2, name=The Chopper, ATTACK=2, creatureType=Mech, HEALTH=1, ATTACK_AVAILABLE=1}
Send to Zomis: ZoneChangeMessage [entity=95, sourceZone=72, destinationZone=73]

if card is already known, send ZoneChange only
if card is not known, send ZoneChange first and then CardInfo

when cards are created from nowhere, ZoneChange with source -1 is sent and then CardInfo
*/
            }
        });
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
                    Container<Actor> container = holders.get(key);
                    if (container == null) {
                        Gdx.app.log("GameScreen", "no container for " + key);
                        return;
                    }
                    Gdx.app.log("GameScreen", "putting zoneview for " + key);
                    container.setActor(zoneView.getActor());
                    zoneViews.put(message.getId(), zoneView);
                }
            }
        });

        return handlers;
    }

    private ZoneView createZoneView(ZoneMessage message) {
        String type = message.getName();
        if (type.equals("Battlefield")) {
            return new DefaultZoneView(context, message, this.entityViews);
        }
        if (type.equals("Hand")) {
            return new DefaultZoneView(context, message, this.entityViews);
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
    
	public boolean checkCardDrop(CardViewSmall cardView) {
		Table table = (Table)cardView.getActor();
		Vector2 stageLoc = table.localToStageCoordinates(new Vector2());
		Rectangle tableRect = new Rectangle(stageLoc.x, stageLoc.y, table.getWidth(), table.getHeight());

		for (Container<Actor> actor : this.holders.values()) {
			if (actor.getName() == "Battlefield") {
				Vector2 stageBattlefieldLoc = actor.localToStageCoordinates(new Vector2(actor.getActor().getX(), actor.getActor().getY()));
				Vector2 modifiedSBL = new Vector2(stageBattlefieldLoc.x - actor.getWidth()/2, stageBattlefieldLoc.y - actor.getHeight()/2);
				Rectangle deckRect = new Rectangle(modifiedSBL.x, modifiedSBL.y, actor.getWidth() * 0.8f, actor.getHeight());
				
				//uncomment this to see the bug where battlefields pop up in strange places
				/*
				Image squareImage = new Image(new Texture(Gdx.files.internal("cardbg.png")));
				squareImage.setPosition(modifiedSBL.x, modifiedSBL.y);
				squareImage.setSize(deckRect.width, deckRect.height);
				this.game.stage.addActor(squareImage);
				*/
				
				if (tableRect.overlaps(deckRect)) {
					//this.addEntity(cardView);
					System.out.println("target found!");
					return true;
				}
			}
		}
		
		return false;
		
		//these can be used to double check the location of the rectangles
		/*
		Image squareImage = new Image(new Texture(Gdx.files.internal("cardbg.png")));
		squareImage.setPosition(modifiedSBL.x, modifiedSBL.y);
		squareImage.setSize(deckRect.width, deckRect.height);
		this.game.stage.addActor(squareImage);
		*/
		/*
		Image squareImage = new Image(new Texture(Gdx.files.internal("cardbg.png")));
		squareImage.setPosition(stageLoc.x, stageLoc.y);
		squareImage.setSize(tableRect.width, tableRect.height);
		this.game.stage.addActor(squareImage);
		*/
	}

	@Override
	public boolean addEntity(EntityView view) {
		//called by the CardViewSmall when not in mulligan mode, nothing will happen
		return false;
	}
}
