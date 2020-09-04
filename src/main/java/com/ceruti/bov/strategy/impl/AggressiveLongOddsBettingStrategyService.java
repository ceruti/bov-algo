package com.ceruti.bov.strategy.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("strategy-aggressive_long_odds")
public class AggressiveLongOddsBettingStrategyService extends AggressiveBettingStrategyService {

    private static final int LOWER_BOUNDARY_FOR_ADDITIONAL_BET = 150;
    private static final int UPPER_BOUNDARY_FOR_ADDITIONAL_BET = 250;

    @Override
    public int getLowerBoundaryForAdditionalBet() { return LOWER_BOUNDARY_FOR_ADDITIONAL_BET; }

    @Override
    public int getUpperBoundaryForAdditionalBet() {
        return UPPER_BOUNDARY_FOR_ADDITIONAL_BET;
    }


}
