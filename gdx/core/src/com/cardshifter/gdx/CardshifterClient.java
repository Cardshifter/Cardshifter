package com.cardshifter.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.Json;
import com.cardshifter.gdx.api.messages.Message;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CardshifterClient implements Runnable {

    private final Socket socket;
    private final OutputStream output;
    private final InputStream input;
    private final ByteTransformer transformer;
    private final CardshifterMessageHandler handler;

    public CardshifterClient(String host, int port, CardshifterMessageHandler handler) {
        socket = Gdx.net.newClientSocket(Net.Protocol.TCP, host, port, new SocketHints());
        output = socket.getOutputStream();
        input = socket.getInputStream();
        transformer = new ByteTransformer();
        this.handler = handler;
        try {
            output.write("{ \"command\": \"serial\", \"type\": \"1\" }".getBytes());
            output.flush();
            Gdx.app.log("Client", "Sent serial type");
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
                Gdx.app.log("Client", "listening for input");
                byte[] bytes = new byte[1024];
                try {
                    int read = dataIn.readInt();
                    dataIn.read(bytes, 0, read);
                    Message message = transformer.readOnce(input);
                    handler.handle(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
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
