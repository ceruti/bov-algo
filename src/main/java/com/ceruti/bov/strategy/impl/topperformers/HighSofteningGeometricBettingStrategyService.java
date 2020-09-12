package com.ceruti.bov.strategy.impl.topperformers;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.impl.variable.VariableAmount_BettingStrategyService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ceruti.bov.BettingFacilitatorService.INIT_BET;

@Profile("strategy-high_softening_geometric")
@Component
@Deprecated
public class HighSofteningGeometricBettingStrategyService extends VariableAmount_BettingStrategyService {

    private static final double SOFTENING_FACTOR = 100.0; // TODO: change this? increasing will decrease typical wager amount

    @Override
    public double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        double baseRiskAmount = INIT_BET * 2.0; // TODO: change this?
        double riskAmount = (baseRiskAmount * Math.pow(winMultiplier(price), 2.0)) / SOFTENING_FACTOR;
        return getAdditionalBetRiskAmount(outcome, price, bettingSession, riskAmount);
    }

}
