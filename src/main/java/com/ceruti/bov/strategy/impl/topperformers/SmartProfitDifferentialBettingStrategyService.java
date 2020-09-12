package com.ceruti.bov.strategy.impl.topperformers;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.impl.variable.VariableAmount_BettingStrategyService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("strategy_smart-profit-differential")
@Component
public class SmartProfitDifferentialBettingStrategyService extends VariableAmount_BettingStrategyService {

    public static final double LESS_PROFITABLE_GEOMETRIC_PENALTY_FACTOR = 1.7; // 1.7 seems to be a good number here
    public static final double MORE_PROFITABLE_GEOMETRIC_PENALTY_FACTOR = 1.8; // this should be between 1.7 and 1.9 --> higher is more "conservative"
    public static double K_MORE_PROFITABLE_OUTCOME = -1.50; // TODO tweak?
    public static double K_LESS_PROFITABLE_OUTCOME = -1.50; // TODO: tweak?
    public static double BASE_ADDITIONAL_BET = .20; // TODO: make this a function of INIT_BET?
    public static double GEOMETRIC_BET_FACTOR = 3.2; // TODO: tweak?
    public static int DIFFERENTIAL_PENALTY_LINEAR_FACTOR = 6000; // TODO: tweak?
    public static final double DIFFERENTIAL_BONUS_LINEAR_FACTOR = DIFFERENTIAL_PENALTY_LINEAR_FACTOR / 8.0; // TODO: tweak?

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        double riskAmount = BASE_ADDITIONAL_BET * Math.pow(winMultiplier(price), GEOMETRIC_BET_FACTOR);
        return getAdditionalBetRiskAmount(outcome, price, bettingSession, riskAmount);
    }

    protected double getAdditionalBetRiskAmount(Outcome outcome, Price price, BettingSession bettingSession, double riskAmount) {
        if (price.getAmerican() < 0) {
            return 0.0;
        }
        double currentMinimumProfit = bettingSession.getMinimumProfit();
        double currentMaximumProfit = bettingSession.getMaximumProfit();
        boolean isMoreProfitableOutcome = bettingSession.getMoreProfitableOutcomeId().equalsIgnoreCase(outcome.getId());
        double penalizedRiskAmount = riskAmount * differentialPenalty( isMoreProfitableOutcome, currentMinimumProfit,
                currentMaximumProfit);
        return penalizedRiskAmount;
    }

    private static double differentialPenalty(boolean isMoreProfitableOutcome, double currentMinimumProfit, double currentMaximumProfit) {
        if (isMoreProfitableOutcome) { // if betting on the more profitable outcome, need to make sure we are respecting the differential between minimum profit and maximum profit
            return differentialPenalty(currentMaximumProfit - currentMinimumProfit, K_MORE_PROFITABLE_OUTCOME, true);
        } // else, if betting on the less profitable outcome, just want to make sure we are not significantly lowering max profit
        return differentialPenalty(currentMaximumProfit, K_LESS_PROFITABLE_OUTCOME, false);
    }

    private static double differentialPenalty(double differential, double exponentialDecayBase, boolean penalizeHighDifferential) {
        double profitRatio;
        if (penalizeHighDifferential) {
            profitRatio =  Math.pow(differential, MORE_PROFITABLE_GEOMETRIC_PENALTY_FACTOR)
                    / (DIFFERENTIAL_PENALTY_LINEAR_FACTOR);
        } else {
            profitRatio = DIFFERENTIAL_BONUS_LINEAR_FACTOR / Math.pow(differential, LESS_PROFITABLE_GEOMETRIC_PENALTY_FACTOR);
        }
        return Math.pow(Math.E, exponentialDecayBase * profitRatio);
    }

}
