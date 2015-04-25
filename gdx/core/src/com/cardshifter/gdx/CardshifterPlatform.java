package com.cardshifter.gdx;

/**
 * Created by Simon on 4/25/2015.
 */
public interface CardshifterPlatform {

    void setupLogging();
    CardshifterClient createClient(String host, int port, CardshifterMessageHandler handler);

    String getTimeString();

}
