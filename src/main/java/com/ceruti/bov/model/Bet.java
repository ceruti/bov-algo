package com.ceruti.bov.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Bet {

    private Price price;
    private Status status;
    private double riskAmount;
    private double winAmount;
    private Date placedAt;
    private String vendorKey;

    public void markPlaced() {
        this.status = Status.PLACED;
        this.placedAt = new Date();
    }

    public void markFailed() {
        this.status = Status.FAILED;
        this.placedAt = new Date();
    }

    public void markNoToken() {
        this.status = Status.NO_TOKEN;
        this.placedAt = new Date();
    }

    public void markTimedOut() {
        this.status = Status.TIMEOUT;
        this.placedAt = new Date();
    }

    enum Status {
        PLACING, PLACED, FAILED, NO_TOKEN, TIMEOUT
    }

    public Bet(Price price, double riskAmount) {
        this.price = price.clone();
        this.riskAmount = riskAmount;
        this.status = Status.PLACING;
        this.winAmount = getWinAmount(price, riskAmount);
    }

    private double getWinAmount(Price price, double riskAmount) {
        if (price.getAmerican() > 0) {
            return riskAmount * (price.getAmerican() / 100.0);
        }
        return riskAmount * Math.abs(100.0 / price.getAmerican());
    }

    public double getNetProfitInWinAmount() {
        return winAmount;
    }

    public double getNetProfitInLoseAmount() {
        return -riskAmount;
    }

    public boolean isPlaced() {
        return status.equals(Status.PLACED);
    }

    public Bet clone() {
        Bet result = new Bet();
        result.price = this.price.clone();
        result.status = this.status;
        result.riskAmount = this.riskAmount;
        result.winAmount = this.winAmount;
        return result;
    }

}
