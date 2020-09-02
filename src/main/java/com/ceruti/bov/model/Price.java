package com.ceruti.bov.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Price {

    private int american;
    private String id;
    private Date created = new Date();

    @JsonIgnore
    private Clock clockAtTimeOfPrice;

    @JsonIgnore
    private String homeScoreAtTimeOfPrice;

    @JsonIgnore
    private String visitorScoreAtTimeOfPrice;

    @JsonIgnore
    private int currentPeriodHomeScoreAtTimeOfPrice;

    @JsonIgnore
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

    public Price clone() {
        Price result = new Price();
        result.american = this.american;
        result.id = this.id;
        result.created = this.created;
        if (this.clockAtTimeOfPrice != null) {
            result.clockAtTimeOfPrice = this.clockAtTimeOfPrice.clone();
        }
        result.homeScoreAtTimeOfPrice = this.homeScoreAtTimeOfPrice;
        result.visitorScoreAtTimeOfPrice = this.visitorScoreAtTimeOfPrice;
        result.currentPeriodHomeScoreAtTimeOfPrice = this.currentPeriodHomeScoreAtTimeOfPrice;
        result.currentPeriodVisitorScoreAtTimeOfPrice = this.currentPeriodVisitorScoreAtTimeOfPrice;
        return result;
    }

}
