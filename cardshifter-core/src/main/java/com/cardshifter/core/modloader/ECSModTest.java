package com.cardshifter.core.modloader;

public class ECSModTest {
    private final String name;
    private final Runnable runnable;

    public ECSModTest(String name, Runnable runnable) {
        this.name = name;
        this.runnable = runnable;
    }

    public String getName() {
        return name;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
