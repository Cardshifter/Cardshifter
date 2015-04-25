package com.cardshifter.gdx.client;

import com.badlogic.gdx.Gdx;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.serial.ByteTransformer;
import com.cardshifter.gdx.CardshifterClient;
import com.cardshifter.gdx.CardshifterMessageHandler;
import com.cardshifter.gdx.GdxLogger;
import com.cardshifter.gdx.GdxReflection;
import com.sksamuel.gwt.websockets.Websocket;
import com.sksamuel.gwt.websockets.WebsocketListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by Simon on 4/25/2015.
 */
public class GWTClient implements CardshifterClient, WebsocketListener {

    private static final String TAG = "Websocket";
    private final Websocket websocket;
    private final CardshifterMessageHandler handler;
    private final ByteTransformer transformer;
    private final LoginMessage loginMessage;

    public GWTClient(String host, int port, CardshifterMessageHandler handler, LoginMessage loginMessage) {
        websocket = new Websocket(host + ":" + port);
        websocket.addListener(this);
        this.handler = handler;
        transformer = new ByteTransformer(new GdxLogger(), new GdxReflection());
        this.loginMessage = loginMessage;
        websocket.open();
    }

    @Override
    public void send(Message message) {
        try {
            byte[] data = transformer.transform(message);
            websocket.send(data);
        } catch (IOException e) {
            Gdx.app.log(TAG, "Error sending " + message, e);
        }
    }

    @Override
    public void onClose() {
        Gdx.app.log(TAG, "Websocket closed");
    }

    @Override
    public void onMessage(String msg) {
        Gdx.app.log(TAG, "Message: " + msg);
        try {
            Message message = transformer.readOnce(new ByteArrayInputStream(msg.getBytes()));
            handler.handle(message);
        } catch (IOException e) {
            Gdx.app.log(TAG, "Read error", e);
        }
    }

    @Override
    public void onOpen() {
        Gdx.app.log(TAG, "Websocket opened");
        send(loginMessage);
  /*      websocket.send("{ \"command\": \"serial\", \"type\": \"1\" }");
        Gdx.app.log("Client", "Sent serial type");*/
        //platform.setupLogging();
    }
}
