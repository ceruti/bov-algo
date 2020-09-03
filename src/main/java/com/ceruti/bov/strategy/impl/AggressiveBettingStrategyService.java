package com.ceruti.bov.strategy.impl;

import com.ceruti.bov.BettingFacilitatorService;
import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.BettingStrategyService;
import org.springframework.context.annotation.Profile;

@Profile("strategy-aggressive")
public class AggressiveBettingStrategyService extends BettingStrategyService {

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        return getAdditionalBetRiskAmount(event, market, outcome, price, bettingSession, BettingFacilitatorService.INIT_BET, 0);
    }

    @Override
    public int getLowerBoundaryForAdditionalBet() {
        return DEFAULT_LOWER_BOUNDARY_FOR_ADDITIONAL_BET;
    }

    @Override
    public int getUpperBoundaryForAdditionalBet() {
        return DFEAULT_UPPER_BOUNDARY_FOR_ADDITIONAL_BET;
    }

    protected double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession, double currentMinimumProfitThreshold, double holdMinimumProfitsAbove) {
        BettingSession theoreticalBettingSession2x = getTheoreticalBettingSession(outcome, price, bettingSession, 2);
        BettingSession theoreticalBettingSession1x = getTheoreticalBettingSession(outcome, price, bettingSession, 1);
        double currentMinimumProfit = bettingSession.getMinimumProfit();
        double theoreticalBettingSession1xMinimumProfit = theoreticalBettingSession1x.getMinimumProfit();
        double theoreticalBettingSession2xMinimumProfit = theoreticalBettingSession2x.getMinimumProfit();
        if (currentMinimumProfit < currentMinimumProfitThreshold) { // TODO: change this?
            // not making money yet -- we need to bet in the "opposite direction"
            if (theoreticalBettingSession2xMinimumProfit >= holdMinimumProfitsAbove || theoreticalBettingSession2xMinimumProfit >= currentMinimumProfit) {
                return BettingFacilitatorService.INIT_BET * 2;
            } else if (theoreticalBettingSession1xMinimumProfit >= holdMinimumProfitsAbove || theoreticalBettingSession1xMinimumProfit >= currentMinimumProfit) {
                return BettingFacilitatorService.INIT_BET;
            }
        } else {
            // we've already profited
            // so we can afford some risk again, hoping it will keep swinging back and forth
            return BettingFacilitatorService.INIT_BET;
        }
        return 0.0;
    }
}
