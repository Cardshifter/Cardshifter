package com.cardshifter.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.cardshifter.api.config.DeckConfig;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.gdx.Callback;
import com.cardshifter.gdx.CardshifterGame;
import com.cardshifter.gdx.TargetStatus;
import com.cardshifter.gdx.TargetableCallback;
import com.cardshifter.gdx.ui.CardshifterClientContext;
import com.cardshifter.gdx.ui.EntityView;
import com.cardshifter.gdx.ui.cards.CardViewSmall;

import java.util.*;

/**
 * Created by Simon on 2/10/2015.
 */
public class DeckBuilderScreen implements Screen, TargetableCallback {

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

    public DeckBuilderScreen(CardshifterGame game, String modName, int gameId, final DeckConfig deckConfig, final Callback<DeckConfig> callback) {
        this.config = deckConfig;
        this.callback = callback;
        this.table = new Table(game.skin);
        this.table.setFillParent(true);
        this.game = game;
        this.context = new CardshifterClientContext(game.skin, gameId, null, game.stage);

        totalLabel = new Label("0/" + config.getMaxSize(), game.skin);
        nameLabel = new Label(deckName, game.skin);

        Map<Integer, CardInfoMessage> data = deckConfig.getCardData();
        cards = new ArrayList<CardInfoMessage>(data.values());
        Collections.sort(cards, new Comparator<CardInfoMessage>() {
            @Override
            public int compare(CardInfoMessage o1, CardInfoMessage o2) {
                return Integer.compare(o1.getId(), o2.getId());
            }
        });
        pageCount = (int) Math.ceil(cards.size() / CARDS_PER_PAGE);
        cardsTable = new Table(game.skin);
        cardsTable.defaults().space(4);
        TextButton doneButton = new TextButton("Done", game.skin);
        doneButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (config.total() >= config.getMinSize() && config.total() <= config.getMaxSize()) {
                    callback.callback(deckConfig);
                }
            }
        });
        
        cardsInDeckList = new VerticalGroup();
        this.cardsInDeckScrollPane = new ScrollPane(cardsInDeckList);
        this.cardsInDeckScrollPane.setScrollingDisabled(true, false);

        table.add(nameLabel);
        table.add(totalLabel).row();

        table.add(cardsTable);
        table.add(this.cardsInDeckScrollPane);
        savedDecks = new List<String>(game.skin);
        savedDecks.addListener(new ActorGestureListener(){
            @Override
            public boolean longPress(Actor actor, float x, float y) {
                loadDeck(savedDecks.getSelected());
                return true;
            }
        });

        HorizontalGroup buttons = new HorizontalGroup();
        this.previousPageButton = addPageButton(buttons, "Previous", -1, game.skin);
        buttons.addActor(doneButton);
        this.nextPageButton = addPageButton(buttons, "Next", 1, game.skin);

        table.row();
        Table savedTable = scanSavedDecks(game, savedDecks, modName);
        if (savedTable != null) {
            table.add(buttons);
            table.add(savedTable);
        }
        else {
            table.add(buttons).colspan(2);
        }

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

            TextButton save = new TextButton("Save", game.skin);
            save.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    final TextField textField = new TextField(deckName, game.skin);
                    Dialog dialog = new Dialog("Deck Name", game.skin) {
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
                    dialog.show(game.stage);
                }
            });
            saveTable.add(savedDecks).colspan(2).fill().row();
            TextButton delete = new TextButton("Delete", game.skin);
            delete.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    FileHandle handle = external.child(savedDecks.getSelected() + ".deck");
                    handle.delete();
                    updateSavedDeckList();
                }
            });
            saveTable.add(save);
            saveTable.add(delete);
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
            CardViewSmall cardView = new CardViewSmall(context, card);
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
    public boolean addEntity(EntityView view) {
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

        DeckCardView view = new DeckCardView(game.skin, id, name);
        cardsInDeckList.addActorAt(index, view);
        return view;
    }

    private void updateLabels() {
        totalLabel.setText(config.total() + "/" + config.getMaxSize());

    }
}
