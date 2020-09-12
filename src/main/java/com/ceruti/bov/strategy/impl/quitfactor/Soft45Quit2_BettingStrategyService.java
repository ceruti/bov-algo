package com.ceruti.bov.strategy.impl.quitfactor;

import com.ceruti.bov.strategy.impl.variable.VariableAmount_QuitWhenAhead_BettingStrategyService;

@Deprecated
public class Soft45Quit2_BettingStrategyService extends VariableAmount_QuitWhenAhead_BettingStrategyService {


    @Override
    public double getSofteningFactor() {
        return 45.0;
    }

    @Override
    public int expectedProfitQuitFactor() {
        return 2;
    }
}
