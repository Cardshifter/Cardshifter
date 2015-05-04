package net.zomis.cardshifter.ecs.usage;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by Simon on 4/24/2015.
 */
public class MixinWelcomeMessage {

    @JsonIgnore
    public boolean isOK() {
        return false;
    }

}
