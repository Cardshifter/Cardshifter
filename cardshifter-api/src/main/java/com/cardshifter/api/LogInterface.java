package com.cardshifter.api;

/**
 * Created by Simon on 4/24/2015.
 */
public interface LogInterface {

    void info(String obj);
    void error(String obj, Throwable throwable);

}
