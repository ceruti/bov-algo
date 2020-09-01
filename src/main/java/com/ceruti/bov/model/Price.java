package com.ceruti.bov.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Price {

    private int american;
    private String id;
    private Date created = new Date();
    private Clock clockAtTimeOfPrice;
    private String homeScoreAtTimeOfPrice;
    private String visitorScoreAtTimeOfPrice;
    private int currentPeriodHomeScoreAtTimeOfPrice;
    private int currentPeriodVisitorScoreAtTimeOfPrice;

    public Price(int american, String id, Clock clockAtTimeOfPrice, String homeScoreAtTimeOfPrice, String visitorScoreAtTimeOfPrice, int currentPeriodHomeScoreAtTimeOfPrice, int currentPeriodVisitorScoreAtTimeOfPrice) {
        this.american = american;
        this.id = id;
        if (clockAtTimeOfPrice != null) {
            this.clockAtTimeOfPrice = clockAtTimeOfPrice.clone();
        }
        this.homeScoreAtTimeOfPrice = homeScoreAtTimeOfPrice;
        this.visitorScoreAtTimeOfPrice = visitorScoreAtTimeOfPrice;
        this.currentPeriodHomeScoreAtTimeOfPrice= currentPeriodHomeScoreAtTimeOfPrice;
        this.currentPeriodVisitorScoreAtTimeOfPrice = currentPeriodVisitorScoreAtTimeOfPrice;
    }

    public void updateCreated() {
        created = new Date();
    }

    public Price clone() {
        Price result = new Price();
        result.american = this.american;
        result.id = this.id;
        result.created = this.created;
        result.clockAtTimeOfPrice = this.clockAtTimeOfPrice.clone();
        result.homeScoreAtTimeOfPrice = this.homeScoreAtTimeOfPrice;
        result.visitorScoreAtTimeOfPrice = this.visitorScoreAtTimeOfPrice;
        result.currentPeriodHomeScoreAtTimeOfPrice = this.currentPeriodHomeScoreAtTimeOfPrice;
        result.currentPeriodVisitorScoreAtTimeOfPrice = this.currentPeriodVisitorScoreAtTimeOfPrice;
        return result;
    }

}
