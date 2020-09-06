package com.ceruti.bov.strategy.impl.topperformers;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.impl.variable.VariableAmount_BettingStrategyService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ceruti.bov.BettingFacilitatorService.INIT_BET;

@Profile("strategy-high_softening_high_threshold")
@Component
public class HighSofteningHighThresholdBettingStrategyService extends VariableAmount_BettingStrategyService {

    private static final int LOWER_BOUNDARY_FOR_ADDITIONAL_BET = 100;
    private static final int UPPER_BOUNDARY_FOR_ADDITIONAL_BET = 600;

    private static final double SOFTENING_FACTOR = 55.0; // TODO: change this? increasing will decrease typical wager amount

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        double baseRiskAmount = INIT_BET * 2.0; // TODO: change this?
        double riskAmount = (baseRiskAmount * winMultiplier(price)) / SOFTENING_FACTOR;
        return getAdditionalBetRiskAmount(outcome, price, bettingSession, riskAmount);
    }

    @Override
    public int getLowerBoundaryForAdditionalBet() { return LOWER_BOUNDARY_FOR_ADDITIONAL_BET; }

    @Override
    public int getUpperBoundaryForAdditionalBet() {
        return UPPER_BOUNDARY_FOR_ADDITIONAL_BET;
    }

}
