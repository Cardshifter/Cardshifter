package com.cardshifter.core;

import com.cardshifter.api.LogInterface;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Created by Simon on 4/24/2015.
 */
public class Log4jAdapter implements LogInterface {

    private static final Logger logger = LogManager.getLogger(Log4jAdapter.class);

    @Override
    public void info(String obj) {
        logger.info(obj);
    }

    @Override
    public void error(String obj, Throwable throwable) {
        logger.error(obj, throwable);
    }
}
