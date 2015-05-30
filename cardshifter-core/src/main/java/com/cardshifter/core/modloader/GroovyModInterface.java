package com.cardshifter.core.modloader;

import com.cardshifter.modapi.base.ECSMod;

import java.util.ArrayList;
import java.util.List;

public interface GroovyModInterface extends ECSMod {

    default List<ECSModTest> getTests() {
        return new ArrayList<>();
    }

}
