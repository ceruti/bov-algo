package com.ceruti.bov.strategy.impl.topperformers;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.impl.variable.VariableAmount_BettingStrategyService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.ceruti.bov.BettingFacilitatorService.INIT_BET;

@Profile("strategy-account-for-minimum-profit-aggressive")
@Component
@Deprecated
public class AccountForCurrentMinimumProfitBettingStrategyServiceAggressive extends AccountForCurrentMinimumProfitBettingStrategyService {

    @Override
    protected boolean improvesMinimumProfit(double currentMinimumProfit, double theoreticalMinimumProfit) {
        return true; // only difference in this strategy is that we don't try to hold minimum profits above 0 once acheived
    }

}
