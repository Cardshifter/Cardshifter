package com.cardshifter.server.stats;

import com.cardshifter.modapi.actions.ActionPerformEvent;
import com.cardshifter.modapi.events.EntityRemoveEvent;
import com.cardshifter.modapi.events.GameOverEvent;
import com.cardshifter.modapi.phase.PhaseChangeEvent;
import com.cardshifter.modapi.phase.PhaseEndEvent;
import com.cardshifter.modapi.phase.PhaseStartEvent;

import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Created by Simon on 2/16/2015.
 */
public class StatsGame {

//    @AssignValue("who")

    Function<ActionPerformEvent, String> action = e -> e.getAction().getName();

    ToIntFunction<PhaseEndEvent> turns = e -> 1;
    ToIntFunction<EntityRemoveEvent> removed = e -> 1;
    ToIntFunction<ActionPerformEvent> actions = e -> 1;

}
