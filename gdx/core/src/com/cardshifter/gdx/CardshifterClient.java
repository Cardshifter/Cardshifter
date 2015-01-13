package com.cardshifter.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.cardshifter.api.messages.Message;
import com.cardshifter.api.serial.ByteTransformer;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;

import java.io.*;

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
            LogManager.getRootLogger().addAppender(new AppenderSkeleton() {
                @Override
                protected void append(LoggingEvent event) {
                    Gdx.app.log(event.getLoggerName(), String.valueOf(event.getMessage()));
                }

                @Override
                public void close() {

                }

                @Override
                public boolean requiresLayout() {
                    return false;
                }
            });
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
                try {
                    Gdx.app.log("Client", "reading x bytes");
                    Message message = transformer.readOnce(dataIn);
/*
                    int read = dataIn.readInt();
                    Gdx.app.log("Client", "reading " + read + " bytes");
                    dataIn.read(bytes, 0, read);
                    Gdx.app.log("Client", "received: " + Arrays.toString(bytes));
                    Message message = transformer.readOnce(new ByteArrayInputStream(bytes, 0, read));
*/
                    Gdx.app.log("Client", "transformed into: " + message);
                    handler.handle(message);
                    Gdx.app.log("Client", "handled complete: " + message);
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
