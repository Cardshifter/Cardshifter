package com.cardshifter.gdx.client;

import com.cardshifter.gdx.CardshifterPlatform;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

/**
 * Created by Simon on 4/25/2015.
 */
public class GWTPlatform implements CardshifterPlatform {

    private final DateTimeFormat format = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_FULL);

    @Override
    public void setupLogging() {

    }

    @Override
    public String getTimeString() {
        return format.format(new Date());
    }

}
