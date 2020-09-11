package com.ceruti.bov.strategy.impl.topperformers;

import com.ceruti.bov.BettingFacilitatorService;
import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.impl.variable.VariableAmount_BettingStrategyService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("strategy-profit-differential")
@Component
public class ProfitDifferentialBettingStrategyService extends VariableAmount_BettingStrategyService {

    private static final double SOFTENING_FACTOR = 300.0; // TODO: higher for hedges against original favorite bet, lower for hedges against original underdog
    private static final double K_NEGATIVE_ODDS = 2.00;
    private static final double K_POSITIVE_ODDS = -4.00;
    public static final double BASE_ADDITIONAL_BET = 20.00;


    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        double baseRiskAmount = BASE_ADDITIONAL_BET * 3.0; // TODO: change this?/
        double riskAmount = (baseRiskAmount * Math.pow(winMultiplier(price), 2.5)) / SOFTENING_FACTOR;
        if (riskAmount > 5.00) {
//            System.out.println("here");
        }
        return getAdditionalBetRiskAmount(outcome, price, bettingSession, riskAmount);
    }

    protected double getAdditionalBetRiskAmount(Outcome outcome, Price price, BettingSession bettingSession, double riskAmount) {
        if (price.getAmerican() == 0) {
            return 0.0;
        }
        BettingSession theoreticalBettingSession = getTheoreticalBettingSession(outcome, price, bettingSession, riskAmount);
        double currentMinimumProfit = bettingSession.getMinimumProfit();
        double currentMaximumProfit = bettingSession.getMaximumProfit();
        double theoreticalMinimumProfit = theoreticalBettingSession.getMinimumProfit();
        double theoreticalMaximumProfit = theoreticalBettingSession.getMaximumProfit();
        boolean isMoreProfitableOutcome = bettingSession.getMoreProfitableOutcomeId().equalsIgnoreCase(outcome.getId());
        if (/*currentMinimumProfit < 0 && theoreticalMinimumProfit < 0 && (theoreticalMinimumProfit > currentMinimumProfit || price.getAmerican() > 0)*/true) {
            if (price.getAmerican() == 575) {
//                System.out.println("here");
            }
            double penalizedRiskAmount = riskAmount * redZonePenalty( isMoreProfitableOutcome, currentMinimumProfit,
                    currentMaximumProfit, theoreticalMinimumProfit, theoreticalMaximumProfit);
            if (penalizedRiskAmount > 5.00) {
                System.out.println("bet exceed $5");
            }
            return penalizedRiskAmount;
        } else if (theoreticalMinimumProfit > ABSOLUTE_MINIMUM_PROFIT && theoreticalMaximumProfit > 0 && (outcome.isForceBettingEnabled() || improvesMinimumProfit(currentMinimumProfit, theoreticalMinimumProfit))) {
            return riskAmount;
        }
        return 0.0;
    }

    protected boolean improvesMinimumProfit(double currentMinimumProfit, double theoreticalMinimumProfit) {
        return theoreticalMinimumProfit >= 0 || theoreticalMinimumProfit >= currentMinimumProfit;
    }

    private static double redZonePenalty(boolean isMoreProfitableOutcome, double currentMinimumProfit, double currentMaximumProfit, double theoreticalMinimumProfit, double theoreticalMaximumProfit) {
        // TODO: tight differentials are penalized when profit is guaranteed:
        // for example: min/max are both around 50
        // should bet aggressively, but will not do so

        // good thing: betting low on tightening high differential when initial outcome is favorite prevents ruining "inital win" scenarios
        // BECAUSE: you are lowering maximum profit

        // bad thing: betting low on tight differential when profits are secured (min/max profit both well above 0) is bad
        // BECAUSE: you are


        double profitDifferential = Math.abs(currentMaximumProfit - currentMinimumProfit);
        double minimumProfitRatio;
        if (isMoreProfitableOutcome) { // higher penalty --> r should be higher as differential increases
            minimumProfitRatio = profitDifferential / BASE_ADDITIONAL_BET * 2;
        } else { // lower penalty --> r should be lower as differential increases
            minimumProfitRatio = BASE_ADDITIONAL_BET  / profitDifferential * 2;
        }
        return Math.pow(Math.E, K_POSITIVE_ODDS * minimumProfitRatio);
    }

}
