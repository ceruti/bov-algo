package com.ceruti.bov.strategy;

import com.ceruti.bov.BetPlacingService;
import com.ceruti.bov.BettingFacilitatorService;
import com.ceruti.bov.model.*;

public abstract class BettingStrategyService {

    protected static final int DEFAULT_LOWER_BOUNDARY_FOR_ADDITIONAL_BET = 100;
    protected static final int DEFAULT_UPPER_BOUNDARY_FOR_ADDITIONAL_BET = 200;

    public BettingStrategyService() {}

    public boolean isWithinBoundariesForAdditionalBet(int americanOdds) {
        return americanOdds > getLowerBoundaryForAdditionalBet() && americanOdds < getUpperBoundaryForAdditionalBet();
    }

    public abstract double getAdditionalBetRiskAmount(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession);

    public abstract int getLowerBoundaryForAdditionalBet();

    public abstract int getUpperBoundaryForAdditionalBet();

    protected static BettingSession getTheoreticalBettingSession(Outcome outcome, Price price, BettingSession bettingSession, int timesInitialBet) {
        BettingSession theoreticalBettingSession = bettingSession.clone();
        Bet theoreticalBet = getTheoreticalBet(price, timesInitialBet);
        theoreticalBettingSession.update(theoreticalBet, outcome.getId());
        return theoreticalBettingSession;
    }

    protected static BettingSession getTheoreticalBettingSession(Outcome outcome, Price price, BettingSession bettingSession, double riskAmount) {
        BettingSession theoreticalBettingSession = bettingSession.clone();
        Bet theoreticalBet = getTheoreticalBet(price, riskAmount);
        theoreticalBettingSession.update(theoreticalBet, outcome.getId());
        return theoreticalBettingSession;
    }

    protected static Bet getTheoreticalBet(Price price, int factor) {
        Bet bet = new Bet(price, BettingFacilitatorService.INIT_BET * factor);
        bet.markPlaced();
        return bet;
    }

    protected static Bet getTheoreticalBet(Price price, double riskAmount) {
        Bet bet = new Bet(price, riskAmount);
        bet.markPlaced();
        return bet;
    }

    protected static int toAmountInCents(double riskAmount) {
        return (int) (Math.ceil(riskAmount * 100));
    }

}
