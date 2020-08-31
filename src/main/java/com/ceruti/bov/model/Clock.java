package com.ceruti.bov.model;

import lombok.Data;

@Data
public class Clock {

    private String period;
    private int periodNumber;
    private int numberOfPeriods;
    private String gameTime;
    private boolean isTicking;
    private String direction;
    private int relativeGameTimeInSeconds;

    public Clock clone() {
        Clock result = new Clock();
        result.period = this.period;
        result.periodNumber = this.periodNumber;
        result.gameTime = this.gameTime;
        result.isTicking = this.isTicking;
        result.numberOfPeriods = this.numberOfPeriods;
        result.direction = this.direction;
        result.relativeGameTimeInSeconds = this.relativeGameTimeInSeconds;
        return result;
    }

}
