package com.ceruti.bov.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
public class SimulationAggregateResultElement {

    private String sport;
    private double netProfit;
    private double averageProfit;
    private double medianProfit;
    private int totalBets;
    private double averageBets;
    private int eventsBetOn;
    private double averageFavoriteReversals;
    private double averageWinnerOddsStandardDeviation;
    private double averageLoserOddsStandardDeviation;
    private double averageNumOddsQuoted;

}
