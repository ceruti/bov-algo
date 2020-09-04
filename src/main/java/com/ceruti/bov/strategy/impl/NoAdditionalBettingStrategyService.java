package com.ceruti.bov.strategy.impl;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.BettingStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// for control group in simulations
@Component
@Profile("strategy-none")
public class NoAdditionalBettingStrategyService extends BettingStrategyService {


    @Autowired
    public NoAdditionalBettingStrategyService() {
        super();
    }

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        return 0.0; // don't bet: this is our control group
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
