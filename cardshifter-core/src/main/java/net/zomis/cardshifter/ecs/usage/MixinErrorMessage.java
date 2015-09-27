package net.zomis.cardshifter.ecs.usage;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface MixinErrorMessage {

    @JsonProperty("cause")
    void setCause(String name);

    @JsonProperty("cause")
    String getStringCause();

}
