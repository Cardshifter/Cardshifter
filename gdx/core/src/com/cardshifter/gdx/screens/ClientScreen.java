package com.cardshifter.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cardshifter.api.both.ChatMessage;
import com.cardshifter.api.both.InviteRequest;
import com.cardshifter.api.both.InviteResponse;
import com.cardshifter.api.both.PlayerConfigMessage;
import com.cardshifter.api.config.DeckConfig;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.ServerQueryMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.AvailableModsMessage;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.api.outgoing.UserStatusMessage;
import com.cardshifter.gdx.*;
import com.cardshifter.gdx.ui.UsersList;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zomis on 2014-11-11.
 */
public class ClientScreen implements Screen, CardshifterMessageHandler {
	
    private final CardshifterClient client;
    private final Map<Class<? extends Message>, SpecificHandler<?>> handlerMap = new HashMap<Class<? extends Message>, SpecificHandler<?>>();
    private final Table table;
    private final CardshifterGame game;
    private final TextArea chatMessages;
    private final TextField messageField;
    private final UsersList usersList;
    private String[] availableMods;
    private GameScreen gameScreen;
    private String currentModName;
    private ScrollPane chatScrollPane;

    public ClientScreen(final CardshifterGame game, String host, int port, final String username) {
    	
        this.game = game;
        client = game.getPlatform().createClient(host, port, this, new LoginMessage(username));
        
        table = new Table(game.skin);
        table.setFillParent(true);

        Label titleLabel = new Label("Welcome to Cardshifter", game.skin);
        table.add(titleLabel);
        Label usersListLabel = new Label("Users Online:", game.skin);
        table.add(usersListLabel).colspan(2);
        table.row();
        
        this.chatMessages = new TextArea("", game.skin);
        this.chatMessages.setDisabled(true);
        this.chatScrollPane = new ScrollPane(this.chatMessages);
        this.chatScrollPane.setScrollingDisabled(true, false);
        table.add(this.chatScrollPane).expand().fill();
        
        usersList = new UsersList(game.skin, new Callback<String>() {
            @Override
            public void callback(String object) {
                currentModName = object;
            }
        });
        ScrollPane userScrollPane = new ScrollPane(usersList.getTable());
        table.add(userScrollPane).top().colspan(2);
        //table.add(usersList.getGroup()).right().expandY().fill().colspan(2);
        table.row();
        
        this.messageField = new TextField("", game.skin);
        this.messageField.addListener(new InputListener() {
            public boolean keyTyped(InputEvent event, char character) {
            	if (character == '\r' || character == '\n') {
            		ClientScreen.this.sendChatMessage();
            		return true;
            	} else {
            		return false;
            	}
            }
        });
        table.add(this.messageField).bottom().expandX().fill();
        
        TextButton sendMessageButton = new TextButton("Send", game.skin);
        sendMessageButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            	ClientScreen.this.sendChatMessage();
            }
        });
        table.add(sendMessageButton).fill();
        
        TextButton inviteButton = new TextButton("Invite", game.skin);
        inviteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                usersList.inviteSelected(availableMods, game.stage, client);
            }
        });
        table.add(inviteButton).fill();
        
        this.configureHandler();

/*        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                client.send(new LoginMessage(username));
            }
        });*/
    }
    
    private void configureHandler() {
        handlerMap.put(AvailableModsMessage.class, new SpecificHandler<AvailableModsMessage>() {
            @Override
            public void handle(AvailableModsMessage message) {
                availableMods = message.getMods();
                client.send(new ServerQueryMessage(ServerQueryMessage.Request.USERS));
            }
        });
        handlerMap.put(ChatMessage.class, new SpecificHandler<ChatMessage>() {
            @Override
            public void handle(ChatMessage message) {
                String time = game.getPlatform().getTimeString();
                String append = "\n" + "[" + time + "] " + message.getFrom() + ": " + message.getMessage();                
                chatMessages.setText(chatMessages.getText() + append);
                int numberOfScrollLines = chatMessages.getText().split("\n").length;
               	chatMessages.setPrefRows(numberOfScrollLines); 
               	ClientScreen.this.chatScrollPane.layout();   
               	ClientScreen.this.chatScrollPane.setScrollY(ClientScreen.this.chatMessages.getHeight());
            }
        });
        handlerMap.put(UserStatusMessage.class, new SpecificHandler<UserStatusMessage>() {
            @Override
            public void handle(UserStatusMessage message) {
                usersList.handleUserStatus(message);
            }
        });
        handlerMap.put(NewGameMessage.class, new SpecificHandler<NewGameMessage>() {
            @Override
            public void handle(NewGameMessage message) {
                gameScreen = new GameScreen(game, client, message, ClientScreen.this);
                handlerMap.putAll(gameScreen.getHandlers());
                game.setScreen(gameScreen);
            }
        });
        handlerMap.put(PlayerConfigMessage.class, new SpecificHandler<PlayerConfigMessage>() {
            @Override
            public void handle(final PlayerConfigMessage message) {
                DeckConfig deckConfig = (DeckConfig) message.getConfigs().get("Deck");
                if (deckConfig != null) {
                    game.setScreen(new DeckBuilderScreen(ClientScreen.this, game, currentModName, message.getGameId(), deckConfig, new Callback<DeckConfig>() {
                        @Override
                        public void callback(DeckConfig object) {
                            game.setScreen(gameScreen);
                            client.send(message);
                        }
                    }));
                }
            }
        });
        handlerMap.put(InviteRequest.class, new SpecificHandler<InviteRequest>() {
            @Override
            public void handle(final InviteRequest message) {
                Dialog dialog = new Dialog("Invite", game.skin) {
                    @Override
                    protected void result(Object object) {
                        boolean response = (Boolean) object;
                        client.send(new InviteResponse(message.getId(), response));
                    }
                };
                currentModName = message.getGameType();
                dialog.text(message.getName() + " invites you to play " + message.getGameType());
                dialog.button("Accept", true);
                dialog.button("Decline", false);
                dialog.show(game.stage);
            }
        });
    }
    
    private void sendChatMessage() {
		String message = this.messageField.getText();
		if (message != null) {
			this.client.send(new ChatMessage(1, "unused", message));
			this.messageField.setText("");
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
    public void handle(final Message message) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                final SpecificHandler<Message> handler = (SpecificHandler<Message>) handlerMap.get(message.getClass());
                if (handler != null) {
                    handler.handle(message);
                }
                else {
                    Gdx.app.log("Client", "WARNING: Unable to handle " + message + " of type " + message.getClass());
                }
            }
        });
    }
}
