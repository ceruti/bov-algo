package com.ceruti.bov.strategy.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("strategy-aggressive_extra_long_odds")
@Deprecated
public class AggressiveExtraLongOddsBettingStrategyService extends AggressiveBettingStrategyService {

    private static final int LOWER_BOUNDARY_FOR_ADDITIONAL_BET = 175;
    private static final int UPPER_BOUNDARY_FOR_ADDITIONAL_BET = 275;

    @Override
    public int getLowerBoundaryForAdditionalBet() { return LOWER_BOUNDARY_FOR_ADDITIONAL_BET; }

    @Override
    public int getUpperBoundaryForAdditionalBet() {
        return UPPER_BOUNDARY_FOR_ADDITIONAL_BET;
    }


}
