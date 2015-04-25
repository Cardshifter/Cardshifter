package com.cardshifter.gdx;

/**
 * Created by Simon on 4/25/2015.
 */
public class NonGWTPlatform implements CardshifterPlatform {

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

}
