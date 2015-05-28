package com.cardshifter.api.config;

/**
 * Indicates that a class is used for Player configuration
 */
public interface PlayerConfig {
    /**
     * Called before sending the Player Configuration request, to make last-minute cleanup in what to send
     */
    void beforeSend();
}
