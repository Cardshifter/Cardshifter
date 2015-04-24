package net.zomis.cardshifter.ecs.usage;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Simon on 4/24/2015.
 */
public class MixinPlayerConfigMessage {

    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "_type")
    private final Map<String, Object> configs = new HashMap<>();

}
