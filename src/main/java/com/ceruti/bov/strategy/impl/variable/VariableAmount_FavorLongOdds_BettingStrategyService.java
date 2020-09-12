package com.ceruti.bov.strategy.impl.variable;

import com.ceruti.bov.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ceruti.bov.BettingFacilitatorService.INIT_BET;

@Profile("strategy-variable_amount_favor_long")
@Component
@Deprecated
public class VariableAmount_FavorLongOdds_BettingStrategyService extends VariableAmount_BettingStrategyService {

    private static final double SOFTENING_FACTOR = 7.0; // TODO: change this? increasing will decrease typical wager amount

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        double baseRiskAmount = INIT_BET * 2.0; // TODO: change this?
        double riskAmount = (baseRiskAmount * winMultiplier(price)) / SOFTENING_FACTOR;
        return getAdditionalBetRiskAmount(outcome, price, bettingSession, riskAmount);
    }
}
