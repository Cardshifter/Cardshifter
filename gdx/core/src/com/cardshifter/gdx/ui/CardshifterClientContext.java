package com.cardshifter.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.gdx.CardshifterClient;

/**
 * Created by Simon on 2/9/2015.
 */
public class CardshifterClientContext {

    private final Skin skin;
    private final int gameId;
    private final CardshifterClient client;

    public CardshifterClientContext(Skin skin, int gameId, CardshifterClient client) {
        this.skin = skin;
        this.gameId = gameId;
        this.client = client;
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
}
