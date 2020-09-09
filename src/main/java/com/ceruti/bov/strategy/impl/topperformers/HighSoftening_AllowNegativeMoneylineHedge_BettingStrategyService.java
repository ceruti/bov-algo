package com.ceruti.bov.strategy.impl.topperformers;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.impl.variable.VariableAmount_BettingStrategyService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ceruti.bov.BettingFacilitatorService.INIT_BET;

@Profile("strategy-high_softening_allow_negative")
@Component
public class HighSoftening_AllowNegativeMoneylineHedge_BettingStrategyService extends VariableAmount_BettingStrategyService {

    private static final double LINEAR_SOFTENING_FACTOR = 1800; // TODO: change this? increasing will decrease typical wager amount
    public static final int LOWER_BOUNDARY_FOR_BET = -175;
    public static final double GEOMETRIC_FACTOR = 3.5;
    public static final int UPPER_BOUNDARY_FOR_BET = 550;

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        double baseRiskAmount = INIT_BET * 2.0; // TODO: change this?
        double riskAmount;
        riskAmount = (baseRiskAmount * Math.pow(winMultiplier(price), GEOMETRIC_FACTOR)) / LINEAR_SOFTENING_FACTOR;
        return getAdditionalBetRiskAmount(outcome, price, bettingSession, riskAmount);
    }

    @Override
    public int getLowerBoundaryForAdditionalBet() {
        return LOWER_BOUNDARY_FOR_BET;
    }

    @Override
    public int getUpperBoundaryForAdditionalBet() {
        return UPPER_BOUNDARY_FOR_BET;
    }

    // TODO: support forceBetting()?
    protected double getAdditionalBetRiskAmount(Outcome outcome, Price price, BettingSession bettingSession, double riskAmount) {
        BettingSession theoreticalBettingSession = getTheoreticalBettingSession(outcome, price, bettingSession, riskAmount);
        double currentMinimumProfit = bettingSession.getMinimumProfit();
        double currentMaximumProfit = bettingSession.getMaximumProfit();
        double theoreticalMinimumProfit = theoreticalBettingSession.getMinimumProfit();
        double theoreticalMaximumProfit = theoreticalBettingSession.getMaximumProfit();

        // instead of betting on negative moneyline frequently, calculate the amount needed to bet to make it put the minimum profit above 0, then ensure maximum profit stays above 0
        if (price.getAmerican() == -105) {
            System.out.println("aa");
        }
        if (price.getAmerican() < 0 && price.getAmerican() > -150 && currentMinimumProfit < 0 && bettingSession.getLessProfitableOutcomeId().equals(outcome.getId())) {
            double riskAmountNeededForZeroMinimumProfit = Math.abs(currentMinimumProfit / winMultiplier(price));
            if (riskAmountNeededForZeroMinimumProfit < currentMaximumProfit) {
                return riskAmountNeededForZeroMinimumProfit;
            }
        }

        if (currentMinimumProfit >= -(2.0 * INIT_BET) && currentMinimumProfit < 0 && price.getAmerican() > 0) {
            return riskAmount;
        }
        if (currentMinimumProfit > 0 && price.getAmerican() > 0) {
            return theoreticalMinimumProfit > 0 ? riskAmount : 0;
        }
        return 0.0;
    }

    private static boolean improvesMinimumProfitInWorstCase(double currentMinimumProfit, double theoreticalMinimumProfit) {
        return currentMinimumProfit < 1.5 * INIT_BET && theoreticalMinimumProfit > currentMinimumProfit;
    }

}
