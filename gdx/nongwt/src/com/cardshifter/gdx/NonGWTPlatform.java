package com.cardshifter.gdx;

import com.cardshifter.api.incoming.LoginMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Simon on 4/25/2015.
 */
public class NonGWTPlatform implements CardshifterPlatform {

    private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void setupLogging() {
/*        LogManager.getRootLogger().addAppender(new AppenderSkeleton() {
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
        });*/
    }

    @Override
    public CardshifterClient createClient(String host, int port, CardshifterMessageHandler handler, LoginMessage loginMessage) {
        return new CardshifterNonGWTClient(this, host, port, handler, loginMessage);
    }

    @Override
    public String getTimeString() {
        return format.format(Calendar.getInstance().getTime());
    }

}
