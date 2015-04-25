package com.cardshifter.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.serial.ByteTransformer;

import java.io.*;

public class CardshifterClient implements Runnable {

    private final Socket socket;
    private final OutputStream output;
    private final InputStream input;
    private final ByteTransformer transformer;
    private final CardshifterMessageHandler handler;

    public CardshifterClient(CardshifterPlatform platform, String host, int port, CardshifterMessageHandler handler) {
        socket = Gdx.net.newClientSocket(Net.Protocol.TCP, host, port, new SocketHints());
        output = socket.getOutputStream();
        input = socket.getInputStream();
        transformer = new ByteTransformer(new GdxLogger());
        this.handler = handler;
        try {
            output.write("{ \"command\": \"serial\", \"type\": \"1\" }".getBytes());
            output.flush();
            Gdx.app.log("Client", "Sent serial type");
            platform.setupLogging();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            DataInputStream dataIn = new DataInputStream(input);
            while (true) {
                try {
                    Message message = transformer.readOnce(dataIn);
                    Gdx.app.log("Client", "Received: " + message);
                    handler.handle(message);
                } catch (Exception e) {
                    Gdx.app.log("Client", "Error inside read loop", e);
                    e.printStackTrace();
                    break;
                }
            }
        }
        catch (Exception ex) {
            Gdx.app.log("Client", "Error outside read loop", ex);
        }
        Gdx.app.log("Client", "Stopped listening");
    }

    public void send(Message message) {
        try {
            transformer.send(message, output);
            Gdx.app.log("Outgoing", message.toString());
            output.flush();
        } catch (Throwable e) {
            Gdx.app.log("Outgoing", "Error " + e);
            e.printStackTrace();
        }
    }
}
