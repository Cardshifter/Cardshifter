package com.cardshifter.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.cardshifter.api.incoming.RequestTargetsMessage;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.outgoing.UsableActionMessage;
import com.cardshifter.gdx.CardshifterClient;

/**
 * Created by Simon on 2/9/2015.
 */
public class CardshifterClientContext {

    private final Skin skin;
    private final int gameId;
    private final CardshifterClient client;
    private final Stage stage;

    public CardshifterClientContext(Skin skin, int gameId, CardshifterClient client, Stage stage) {
        this.skin = skin;
        this.gameId = gameId;
        this.client = client;
        this.stage = stage;
    }

    public Skin getSkin() {
        return skin;
    }

    public int getGameId() {
        return gameId;
    }

    public void send(Message message) {
        client.send(message);
    }

    public void sendAction(UsableActionMessage action) {
        if (action.isTargetRequired()) {
            send(new RequestTargetsMessage(gameId, action.getId(), action.getAction()));
        }
        else {
            send(new UseAbilityMessage(gameId, action.getId(), action.getAction(), 0));
        }
    }

    public Stage getStage() {
        return stage;
    }
}
