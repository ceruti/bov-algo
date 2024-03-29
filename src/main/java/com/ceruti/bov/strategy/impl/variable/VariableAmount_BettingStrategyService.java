package com.ceruti.bov.strategy.impl.variable;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.BettingStrategyService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ceruti.bov.BettingFacilitatorService.INIT_BET;

public abstract class VariableAmount_BettingStrategyService extends BettingStrategyService {

    private static final double SOFTENING_FACTOR = 5.0; // TODO: change this? increasing will decrease typical wager amount
    private static final int UPPER_BOUNDARY_FOR_ADDITIONAL_BET = 2000; // need a higher bound than usual for variale strategies
    private static final int EFFECTIVE_ODDS_MAX = 400;
    private static final double BASE_BET_AMOUNT = 20.0;
    public static final double ABSOLUTE_MINIMUM_PROFIT = -2.0 * INIT_BET;

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        double riskAmount = (INIT_BET * winMultiplier(price)) / SOFTENING_FACTOR;
        return getAdditionalBetRiskAmount(outcome, price, bettingSession, riskAmount);
    }

    protected double getAdditionalBetRiskAmount(Outcome outcome, Price price, BettingSession bettingSession, double riskAmount) {
        BettingSession theoreticalBettingSession = getTheoreticalBettingSession(outcome, price, bettingSession, riskAmount);
        double currentMinimumProfit = bettingSession.getMinimumProfit();
        double theoreticalMinimumProfit = theoreticalBettingSession.getMinimumProfit();
        double theoreticalMaximumProfit = theoreticalBettingSession.getMaximumProfit();
        if (theoreticalMinimumProfit > ABSOLUTE_MINIMUM_PROFIT && theoreticalMaximumProfit > 0 && (outcome.isForceBettingEnabled() || (theoreticalMinimumProfit >= 0 || theoreticalMinimumProfit >= currentMinimumProfit))) {
            return riskAmount;
        }
        return 0.0;
    }

    protected static double winMultiplier(Price price) {
        if (price.getAmerican() > 0) {
            return Math.min(price.getAmerican(), EFFECTIVE_ODDS_MAX) / 100.0;
        }
        return Math.abs(100.0 / price.getAmerican());
    }

    @Override
    public int getLowerBoundaryForAdditionalBet() {
        return DEFAULT_LOWER_BOUNDARY_FOR_ADDITIONAL_BET;
    }

    @Override
    public int getUpperBoundaryForAdditionalBet() {
        return UPPER_BOUNDARY_FOR_ADDITIONAL_BET;
    }
}
