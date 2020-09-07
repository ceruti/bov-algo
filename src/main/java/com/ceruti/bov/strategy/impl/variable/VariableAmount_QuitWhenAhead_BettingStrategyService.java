package com.ceruti.bov.strategy.impl.variable;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.BettingStrategyService;

import static com.ceruti.bov.BettingFacilitatorService.INIT_BET;

public abstract class VariableAmount_QuitWhenAhead_BettingStrategyService extends VariableAmount_BettingStrategyService {

    private static final double SOFTENING_FACTOR = 5.0; // TODO: change this? increasing will decrease typical wager amount
    private static final int UPPER_BOUNDARY_FOR_ADDITIONAL_BET = 400; // need a higher bound than usual for variale strategies

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        if (market.getExpectedProfit() > expectedProfitQuitFactor() * INIT_BET) {
            return 0.0;
        }
        double riskAmount = (INIT_BET * winMultiplier(price)) / getSofteningFactor();
        return getAdditionalBetRiskAmount(outcome, price, bettingSession, riskAmount);
    }

    public abstract double getSofteningFactor();

    public abstract int expectedProfitQuitFactor();


}
