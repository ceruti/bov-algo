package com.ceruti.bov.model;

import lombok.Data;

import java.util.List;

@Data
public class Outcome {

    private String competitorId;
    private String description;
    private String id;
    private String status;
    private String type;
    private Price price;
    private List<Price> previousPrices;

    private String opposingOutcomeId;

    private boolean bettingEnabled = true;
    private boolean forceBettingEnabled = false;

    public void enableBetting() {
        this.bettingEnabled = true;
    }

    public void disableBetting() {
        this.bettingEnabled = false;
    }

}
