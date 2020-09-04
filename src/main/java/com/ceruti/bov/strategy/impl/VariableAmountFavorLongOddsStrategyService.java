package com.ceruti.bov.strategy.impl;

import com.ceruti.bov.BettingFacilitatorService;
import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.BettingStrategyService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ceruti.bov.BettingFacilitatorService.INIT_BET;

@Profile("strategy-variable_amount_favor_long")
@Component
public class VariableAmountFavorLongOddsStrategyService extends BettingStrategyService {

    private static final double SOFTENING_FACTOR = 5.0; // TODO: change this? increasing will decrease typical wager amount
    private static final int UPPER_BOUNDARY_FOR_ADDITIONAL_BET = 400;

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        double riskAmount = (INIT_BET * winMultiplier(price)) / SOFTENING_FACTOR;
        BettingSession theoreticalBettingSession = getTheoreticalBettingSession(outcome, price, bettingSession, riskAmount);
        double currentMinimumProfit = bettingSession.getMinimumProfit();
        double theoreticalMinimumProfit = theoreticalBettingSession.getMinimumProfit();
        if (currentMinimumProfit < INIT_BET) {
            // not making money yet -- we need to bet in the "opposite direction"
            if (theoreticalMinimumProfit >= 0 || theoreticalMinimumProfit >= currentMinimumProfit) {
                return riskAmount;
            }
        } else {
            // we've already profited
            // so we can afford some risk again, hoping it will keep swinging back and forth
            return riskAmount;
        }
        return 0.0;
    }

    private static double winMultiplier(Price price) {
        if (price.getAmerican() > 0) {
            return price.getAmerican() / 100.0;
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
