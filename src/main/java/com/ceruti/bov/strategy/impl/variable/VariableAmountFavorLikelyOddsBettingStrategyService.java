package com.ceruti.bov.strategy.impl.variable;

import com.ceruti.bov.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ceruti.bov.BettingFacilitatorService.INIT_BET;

@Profile("strategy-variable_amount_favor_likely")
@Component
public class VariableAmountFavorLikelyOddsBettingStrategyService extends VariableAmountBettingStrategyService {

    private static final double SOFTENING_FACTOR = 2.0; // TODO: change this? increasing will decrease typical wager amount, which effectively means more bets per session

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        double baseRiskAmount = INIT_BET * 2.0;
        double riskAmount = baseRiskAmount / (SOFTENING_FACTOR * winMultiplier(price));
        return getAdditionalBetRiskAmount(outcome, price, bettingSession, riskAmount);
    }
}
