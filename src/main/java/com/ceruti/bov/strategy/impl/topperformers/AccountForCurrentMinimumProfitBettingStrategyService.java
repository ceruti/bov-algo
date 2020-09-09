package com.ceruti.bov.strategy.impl.topperformers;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.impl.variable.VariableAmount_BettingStrategyService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ceruti.bov.BettingFacilitatorService.INIT_BET;

@Profile("strategy-account-for-minimum-profit")
@Component
public class AccountForCurrentMinimumProfitBettingStrategyService extends VariableAmount_BettingStrategyService {

    private static final double SOFTENING_FACTOR = 100.0; // TODO: change this? increasing will decrease typical wager amount
    private static final double K_NEGATIVE_ODDS = 2.00;
    private static final double K_POSITIVE_ODDS = -3.50;


    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        double baseRiskAmount = INIT_BET * 3.0; // TODO: change this?/
        double riskAmount = (baseRiskAmount * Math.pow(winMultiplier(price), 2.5)) / SOFTENING_FACTOR;
        return getAdditionalBetRiskAmount(outcome, price, bettingSession, riskAmount);
    }

    protected static double getAdditionalBetRiskAmount(Outcome outcome, Price price, BettingSession bettingSession, double riskAmount) {
        BettingSession theoreticalBettingSession = getTheoreticalBettingSession(outcome, price, bettingSession, riskAmount);
        double currentMinimumProfit = bettingSession.getMinimumProfit();
        double theoreticalMinimumProfit = theoreticalBettingSession.getMinimumProfit();
        double theoreticalMaximumProfit = theoreticalBettingSession.getMaximumProfit();
        boolean isMoreProfitableOutcome = bettingSession.getMoreProfitableOutcomeId().equalsIgnoreCase(outcome.getId());
        if (!isMoreProfitableOutcome) {
            System.out.println("here");
        }
        if (currentMinimumProfit < 0 && (theoreticalMinimumProfit > currentMinimumProfit || price.getAmerican() > 0)) {
            double penalizedRiskAmount = riskAmount * redZonePenalty( price.getAmerican() > 0, isMoreProfitableOutcome, currentMinimumProfit);
            return penalizedRiskAmount;
        } else if (theoreticalMinimumProfit > ABSOLUTE_MINIMUM_PROFIT && theoreticalMaximumProfit > 0 && (outcome.isForceBettingEnabled() || (theoreticalMinimumProfit >= 0 || theoreticalMinimumProfit >= currentMinimumProfit))) {
            return riskAmount;
        }
        return 0.0;
    }

    private static double redZonePenalty(boolean positiveOdds, boolean isMoreProfitableOutcome, double currentMinimumProfit) {
        if (!isMoreProfitableOutcome) {
            System.out.println("here");
        }
        double minimumProfitRatio = Math.abs(currentMinimumProfit / INIT_BET);
        if (!isMoreProfitableOutcome && positiveOdds) {
            return 1.0;
        }
        if (!positiveOdds) {
            return Math.pow(Math.E, K_NEGATIVE_ODDS * minimumProfitRatio);
        }
        return Math.pow(Math.E, K_POSITIVE_ODDS * minimumProfitRatio);
    }

}
