package com.ceruti.bov.strategy.impl.variable;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("strategy-variable_amount_favor_long_high_threshold")
@Component
public class VariableAmount_FavorLongOdds_HighThreshold_BettingStrategyService extends VariableAmount_FavorLongOdds_BettingStrategyService {

    private static final double SOFTENING_FACTOR = 5.0; // TODO: change this? increasing will decrease typical wager amount

    private static final int LOWER_BOUNDARY_FOR_ADDITIONAL_BET = 150;
    private static final int UPPER_BOUNDARY_FOR_ADDITIONAL_BET = 250;

    @Override
    public int getLowerBoundaryForAdditionalBet() { return LOWER_BOUNDARY_FOR_ADDITIONAL_BET; }

    @Override
    public int getUpperBoundaryForAdditionalBet() {
        return UPPER_BOUNDARY_FOR_ADDITIONAL_BET;
    }
}
