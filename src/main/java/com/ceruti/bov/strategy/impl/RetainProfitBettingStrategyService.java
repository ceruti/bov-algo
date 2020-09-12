package com.ceruti.bov.strategy.impl;

import com.ceruti.bov.BettingFacilitatorService;
import com.ceruti.bov.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("strategy-retain_profits")
@Deprecated
public class RetainProfitBettingStrategyService extends AggressiveBettingStrategyService {

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        return super.getAdditionalBetRiskAmount(event, market, outcome, price, bettingSession, BettingFacilitatorService.INIT_BET*2, BettingFacilitatorService.INIT_BET);
    }

    @Override
    public int getLowerBoundaryForAdditionalBet() {
        return DEFAULT_LOWER_BOUNDARY_FOR_ADDITIONAL_BET;
    }

    @Override
    public int getUpperBoundaryForAdditionalBet() {
        return DEFAULT_UPPER_BOUNDARY_FOR_ADDITIONAL_BET;
    }
}
