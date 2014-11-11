package com.cardshifter.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.Json;

import java.io.IOException;

/**
 * Created by Zomis on 2014-11-11.
 */
public class ClientScreen implements Screen {
    private final Socket socket;

    public ClientScreen(CardshifterGame game, String host, int port) {
        socket = Gdx.net.newClientSocket(Net.Protocol.TCP, host, port, new SocketHints());
        try {
            socket.getOutputStream().write("{ \"command\": \"login\", \"username\": \"LibGDX\" }".getBytes());
        } catch (IOException e) {
            Gdx.app.log("client", "Error sending data.", e);
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

    }

    @Override
    public void hide() {

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
}
