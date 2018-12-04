package nl.probotix.helpers;

/**
 * Copyright 2018 (c) ProBotiX
 */
public enum HeavyLiftStages {

    IN(0), OUT(17950);

    private int ticks;

    HeavyLiftStages(int ticks) {
        this.ticks = ticks;
    }

    public int getTicks() {
        return ticks;
    }
}
