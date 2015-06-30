package com.cardshifter.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.SnapshotArray;
import com.cardshifter.api.config.DeckConfig;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.gdx.Callback;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;
import com.cardshifter.gdx.ZoomCardCallback;
import com.cardshifter.gdx.ui.CardshifterClientContext;
import com.cardshifter.gdx.ui.EntityView;
import com.cardshifter.gdx.ui.cards.CardViewSmall;

import java.util.*;

/**
 * Created by Simon on 2/10/2015.
 */
public class DeckBuilderScreen implements Screen, TargetableCallback, ZoomCardCallback {

    private static final int ROWS_PER_PAGE = 3;
    private static final int CARDS_PER_ROW = 4;
    private static final int CARDS_PER_PAGE = CARDS_PER_ROW * ROWS_PER_PAGE;

    private final Callback<DeckConfig> callback;
    private final Table table;
    private final CardshifterGame game;
    private final java.util.List<CardInfoMessage> cards;
    private final DeckConfig config;
    private final int pageCount;
    private final Table cardsTable;
    private final CardshifterClientContext context;
    private final Map<Integer, Label> countLabels = new HashMap<Integer, Label>();
    private final VerticalGroup cardsInDeckList;
    private final ScrollPane cardsInDeckScrollPane;
    private final List<String> savedDecks;
    private final Label nameLabel;
    private final TextButton previousPageButton;
    private final TextButton nextPageButton;
    private int page;
    private String deckName = "unnamed";
    private FileHandle external;
    private final Label totalLabel;
    final Screen lobbyScreen;
    
    private boolean cardZoomedIn = false;
    private float initialCardViewWidth = 0;
    private float initialCardViewHeight = 0;

    public DeckBuilderScreen(Screen screen, CardshifterGame game, String modName, int gameId, final DeckConfig deckConfig, final Callback<DeckConfig> callback) {
        this.config = deckConfig;
        this.callback = callback;
        this.lobbyScreen = screen;
        this.game = game;
        this.context = new CardshifterClientContext(game.skin, gameId, null, game.stage);
        
        Map<Integer, CardInfoMessage> data = deckConfig.getCardData();
        cards = new ArrayList<CardInfoMessage>(data.values());
        Collections.sort(cards, new Comparator<CardInfoMessage>() {
            @Override
            public int compare(CardInfoMessage o1, CardInfoMessage o2) {
                return Integer.compare(o1.getId(), o2.getId());
            }
        });
        pageCount = (int) Math.ceil(cards.size() / CARDS_PER_PAGE);
        	
        //normally once i start constructing libGDX UI elements, I will use a separate method 
        //not doing that here in order have the fields be final
        //this.buildScreen();
        
        TextButton backToMenu = new TextButton("Back to menu", game.skin);
        backToMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            	DeckBuilderScreen.this.game.stage.clear();
                DeckBuilderScreen.this.game.setScreen(DeckBuilderScreen.this.lobbyScreen);
            }
        });
        backToMenu.setPosition(0, Gdx.graphics.getHeight() - Gdx.graphics.getHeight()/20);
        this.game.stage.addActor(backToMenu);
       
        this.table = new Table(game.skin);
        this.table.setFillParent(true);

        totalLabel = new Label("0/" + config.getMaxSize(), game.skin);
        nameLabel = new Label(deckName, game.skin);
        table.add(nameLabel);
        table.add(totalLabel);
        table.row();
        
        cardsTable = new Table(game.skin);
        cardsTable.defaults().space(4); 
        table.add(cardsTable);
        
        cardsInDeckList = new VerticalGroup();
        cardsInDeckList.align(Align.left);
        this.cardsInDeckScrollPane = new ScrollPane(cardsInDeckList);
        this.cardsInDeckScrollPane.setScrollingDisabled(true, false);
        table.add(this.cardsInDeckScrollPane).width(Gdx.graphics.getWidth()/4);
        
        savedDecks = new List<String>(game.skin);
        savedDecks.addListener(new ActorGestureListener(){
            @Override
            public boolean longPress(Actor actor, float x, float y) {
                loadDeck(savedDecks.getSelected());
                return true;
            }
        });
        Table savedTable = scanSavedDecks(game, savedDecks, modName);
        savedTable.setHeight(Gdx.graphics.getHeight());
        ScrollPane savedTableScroll = new ScrollPane(savedTable);
        savedTableScroll.setScrollingDisabled(false, false);
        table.add(savedTableScroll).top();
        
        table.row();

        HorizontalGroup prevNextButtons = new HorizontalGroup();
        this.previousPageButton = addPageButton(prevNextButtons, "Previous", -1, game.skin);
        this.nextPageButton = addPageButton(prevNextButtons, "Next", 1, game.skin);
        table.add(prevNextButtons);
        
        TextButton startGameButton = new TextButton("Start Game", game.skin);
        startGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (config.total() >= config.getMinSize() && config.total() <= config.getMaxSize()) {
                	DeckBuilderScreen.this.game.stage.clear();
                    callback.callback(deckConfig);
                }
            }
        });
        table.add(startGameButton);
        
        TextButton save = new TextButton("Save", game.skin);
        save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final TextField textField = new TextField(deckName, DeckBuilderScreen.this.game.skin);
                Dialog dialog = new Dialog("Deck Name", DeckBuilderScreen.this.game.skin) {
                    @Override
                    protected void result(Object object) {
                        boolean result = (Boolean) object;
                        if (!result) {
                            return;
                        }
                        deckName = textField.getText();
                        saveDeck(deckName);
                    }
                };
                dialog.add(textField);
                dialog.button("Save", true);
                dialog.button("Cancel", false);
                dialog.show(DeckBuilderScreen.this.game.stage);
            }
        });
        TextButton load = new TextButton("Load", game.skin);
        load.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            	DeckBuilderScreen.this.loadDeck(DeckBuilderScreen.this.savedDecks.getSelected());
            }
        });
        TextButton delete = new TextButton("Delete", game.skin);
        delete.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Dialog dialog = new Dialog("Confirm Delete", DeckBuilderScreen.this.game.skin) {
                    @Override
                    protected void result(Object object) {
                        boolean result = (Boolean) object;
                        if (!result) {
                            return;
                        }
                        FileHandle handle = external.child(savedDecks.getSelected() + ".deck");
                        handle.delete();
                        updateSavedDeckList();
                    }
                };
                dialog.button("Delete", true);
                dialog.button("Cancel", false);
                dialog.show(DeckBuilderScreen.this.game.stage);

            }
        });
        HorizontalGroup saveButtons = new HorizontalGroup();
        saveButtons.addActor(save);
        saveButtons.addActor(load);
        saveButtons.addActor(delete);
        table.add(saveButtons);
        
        displayPage(1);
    }

    private Table scanSavedDecks(final CardshifterGame game, final List<String> savedDecks, String modName) {
        if (Gdx.files.isExternalStorageAvailable()) {
            Table saveTable = new Table();
            external = Gdx.files.external("Cardshifter/decks/" + modName + "/");
            external.mkdirs();

            if (!external.exists()) {
                Gdx.app.log("Files", external.path() + " does not exist.");
                return null;
            }

            updateSavedDeckList();

            saveTable.add(savedDecks).colspan(2).fill().row();
            return saveTable;
        }
        return null;
    }

    private void updateSavedDeckList() {
        java.util.List<String> list = new ArrayList<String>();
        for (FileHandle handle : external.list()) {
            if (!handle.isDirectory()) {
                list.add(handle.nameWithoutExtension());
            }
        }
        savedDecks.setItems(list.toArray(new String[list.size()]));
    }

    private void saveDeck(String deckName) {
        FileHandle handle = external.child(deckName + ".deck");
        StringBuilder str = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : config.getChosen().entrySet()) {
            int count = entry.getValue();
            int id = entry.getKey();
            for (int i = 0; i < count; i++) {
                if (str.length() > 0) {
                    str.append(',');
                }
                str.append(id);
            }
        }
        String deckString = str.toString();
        savedDecks.getItems().add(deckName);
        handle.writeString(deckString, false);
        nameLabel.setText(deckName);
        updateSavedDeckList();
    }

    private void loadDeck(String deckName) {
        FileHandle handle = external.child(deckName + ".deck");
        String deckString = handle.readString();
        config.clearChosen();
        this.cardsInDeckList.clear();
        for (String id : deckString.split(",")) {
            try {
                int cardId = Integer.parseInt(id);
                if (config.getCardData().get(cardId) != null) {
                    config.add(cardId);
                }
            }
            catch (NumberFormatException ex) {
            }
        }
        for (Map.Entry<Integer, Label> ee : countLabels.entrySet()) {
            ee.getValue().setText(countText(ee.getKey()));
        }
        nameLabel.setText(deckName);

        for (Map.Entry<Integer, Integer> ee : config.getChosen().entrySet()) {
            DeckCardView cardView = labelFor(ee.getKey());
            cardView.setCount(ee.getValue());
        }
        updateLabels();
    }

    private String countText(int id) {
        Integer value = config.getChosen().get(id);
        return countText(id, value == null ? 0 : value);
    }

    private String countText(int id, int count) {
        int max = config.getMaxFor(id);
        return count + "/" + max;
    }

    private TextButton addPageButton(Group table, String text, final int i, Skin skin) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                displayPage(page + i);
            }
        });
        table.addActor(button);
        return button;
    }

    private void displayPage(int page) {
        this.page = page;
        countLabels.clear();
        int startIndex = (page - 1) * CARDS_PER_PAGE;
        cardsTable.clearChildren();
        for (int i = startIndex; i < startIndex + CARDS_PER_PAGE; i++) {
            if (cards.size() <= i) {
                break;
            }
            if (i % CARDS_PER_ROW == 0) {
                cardsTable.row();
            }
            CardInfoMessage card = cards.get(i);
            VerticalGroup choosableGroup = new VerticalGroup();
            CardViewSmall cardView = new CardViewSmall(context, card, this);
            cardView.setTargetable(TargetStatus.TARGETABLE, this);
            choosableGroup.addActor(cardView.getActor());
            Label label = new Label(countText(card.getId()), game.skin);
            countLabels.put(card.getId(), label);
            choosableGroup.addActor(label);
            cardsTable.add(choosableGroup);
        }
        setButtonEnabled(previousPageButton, page > 1);
        setButtonEnabled(nextPageButton, page <= pageCount);
    }

    private void setButtonEnabled(TextButton button, boolean enabled) {
        if (enabled) {
            button.setTouchable(Touchable.enabled);
            button.setStyle(game.skin.get(TextButton.TextButtonStyle.class));
        }
        else {
            button.setTouchable(Touchable.disabled);
            button.setStyle(game.skin.get("disabled", TextButton.TextButtonStyle.class));
        }
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
    public boolean addEntity(final EntityView view) {
    	
    	//this is always called after a card is clicked
    	//so it is a good place to cancel zoom
    	//and prevent other cards from being added when one is zoomed in on
    	if (this.cardZoomedIn) {
    		if (((CardViewSmall)view).isZoomed){
        		SequenceAction sequence = new SequenceAction();
        		Runnable endZoom = new Runnable() {
        		    @Override
        		    public void run() {
        		    	((CardViewSmall)view).endZoom();
        		    	((CardViewSmall)view).getActor().remove();
        		    	DeckBuilderScreen.this.cardZoomedIn = false;
        		    }
        		};
        		sequence.addAction(Actions.sizeTo(this.initialCardViewWidth, this.initialCardViewHeight, 0.2f));
        		sequence.addAction(Actions.run(endZoom));
        		((CardViewSmall)view).getActor().addAction(sequence);
    		}
    		return false;
    	}
    	
        final int id = view.getId();
        int max = config.getMaxFor(id);
        Integer chosen = config.getChosen().get(id);
        if (chosen == null) {
            chosen = 0;
        }
        int newChosen = (chosen + 1) % (max + 1);
        if (config.total() >= config.getMaxSize() && chosen > 0) {
            newChosen = 0;
        }

        config.setChosen(id, newChosen);
        countLabels.get(id).setText(countText(id, newChosen));
        DeckCardView cardView = labelFor(id);
        cardView.setCount(newChosen);
        updateLabels();

        return true;
    }

    private DeckCardView labelFor(int id) {
        SnapshotArray<Actor> children = cardsInDeckList.getChildren();
        int index = 0;
        String name = (String) config.getCardData().get(id).getProperties().get("name");
        for (Actor actor : children) {
            DeckCardView view = (DeckCardView) actor;
            if (view.getId() == id) {
                return view;
            }
            if (name.compareTo(view.getName()) < 0) {
                break;
            }
            index++;
        }

        DeckCardView view = new DeckCardView(game.skin, id, name, this);
        cardsInDeckList.addActorAt(index, view);
        return view;
    }

    private void updateLabels() {
        totalLabel.setText(config.total() + "/" + config.getMaxSize());

    }
    
    public void removeCardFromDeck(int id) {
    	for (Actor actor : cardsInDeckList.getChildren()) {
    		if (actor instanceof DeckCardView) {
    			if (actor instanceof DeckCardView) {
    				if (((DeckCardView)actor).getId() == id) {
        				int newCount = ((DeckCardView)actor).getCount() - 1;
        				if (newCount > 0) {
        					((DeckCardView) actor).setCount(newCount);
         				} else {
         					actor.remove();
         				}
        				config.setChosen(id, newCount);
    				}
    			}
    		}
    	}
    	this.updateLabels();
    	this.displayPage(this.page);
    }

	@Override
	public void zoomCard(final CardViewSmall cardView) {
		final CardViewSmall cardViewCopy = new CardViewSmall(this.context, cardView.cardInfo, this);
		cardViewCopy.setTargetable(TargetStatus.TARGETABLE, this);
		cardViewCopy.getActor().setPosition(Gdx.graphics.getWidth()/3, Gdx.graphics.getHeight()/3);
		this.game.stage.addActor(cardViewCopy.getActor());
		this.initialCardViewWidth = cardView.getActor().getWidth();
		this.initialCardViewHeight = cardView.getActor().getHeight();
		SequenceAction sequence = new SequenceAction();
		Runnable adjustForZoom = new Runnable() {
		    @Override
		    public void run() {
		    	cardViewCopy.zoom();
		    }
		};
		sequence.addAction(Actions.sizeTo(Gdx.graphics.getWidth()/3, Gdx.graphics.getHeight()/2, 0.2f));
		sequence.addAction(Actions.run(adjustForZoom));		
		cardViewCopy.getActor().addAction(sequence);
		this.cardZoomedIn = true;
	}
}
