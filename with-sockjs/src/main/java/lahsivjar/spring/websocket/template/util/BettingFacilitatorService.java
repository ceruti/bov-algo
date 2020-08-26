package lahsivjar.spring.websocket.template.util;

import lahsivjar.spring.websocket.template.BetPlacingService;
import lahsivjar.spring.websocket.template.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BettingFacilitatorService {

    private static final double INIT_BET = 5.00; // TODO: update amounts?
    public static final int LOWER_BOUND_MONEYLINE_ENTRY = 100;
    public static final int UPPERBOUND_MONEYLINE_ENTRY = 200;

    private BetPlacingService betPlacingService;

    @Autowired
    public BettingFacilitatorService(BetPlacingService betPlacingService) {
        this.betPlacingService = betPlacingService;
    }

    private void updateBettingSessionBasic(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price) {
        if (event.hasAnotherBettingSession(market.getId())) {
            return;
        }
        BettingSession bettingSession = market.getBettingSession();
        if (bettingSession == null && price.getAmerican() > LOWER_BOUND_MONEYLINE_ENTRY && price.getAmerican() < UPPERBOUND_MONEYLINE_ENTRY) {
            attemptInitBettingSession(event, market, outcome, opposingOutcome, price);
        }
        else if (bettingSession != null && price.getAmerican() > LOWER_BOUND_MONEYLINE_ENTRY && price.getAmerican() < UPPERBOUND_MONEYLINE_ENTRY) {
            attemptPlaceAdditionalBet(event, market, outcome, price, bettingSession);
        }
    }

    private void attemptPlaceAdditionalBet(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        BettingSession theoreticalBettingSession2x = getTheoreticalBettingSession(outcome, price, bettingSession, 2);
        BettingSession theoreticalBettingSession1x = getTheoreticalBettingSession(outcome, price, bettingSession, 1);
        if (bettingSession.getMinimumProfit() < INIT_BET) {
            // not making money yet -- we need to bet in the opposite direction
            if (theoreticalBettingSession2x.getMinimumProfit() >= bettingSession.getMinimumProfit()) {
                attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, INIT_BET * 2);
            } else if (theoreticalBettingSession1x.getMinimumProfit() >= bettingSession.getMinimumProfit()) {
                attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, INIT_BET);
           }
       } else {
            // we've already profited
            // so we can afford some risk again, hoping it will keep swinging back and forth
            attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, INIT_BET);
       }
    }

    private void attemptPlaceBetUpdate(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession, double riskAmount) {
        Bet bet = betPlacingService.placeBet(outcome, price, riskAmount);
        if (bet.isPlaced()) {
            bettingSession.update(bet, outcome.getId());
            printBettingSessionUpdate(event, market, outcome, price, market.getBettingSession(), bet);
        }
    }

    private void attemptInitBettingSession(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price) {
        Bet bet = betPlacingService.placeBet(outcome, price, INIT_BET);
        if (bet.isPlaced()) {
            market.initBettingSession(bet, outcome.getId(), opposingOutcome.getId());
            printBettingSessionUpdate(event, market, outcome, price, market.getBettingSession(), bet);
        }
    }

    private BettingSession getTheoreticalBettingSession(Outcome outcome, Price price, BettingSession bettingSession, int factor) {
        BettingSession theoreticalBettingSession2x = bettingSession.clone();
        Bet theoreticalBet2X = getTheoreticalBet(price, factor);
        theoreticalBettingSession2x.update(theoreticalBet2X, outcome.getId());
        return theoreticalBettingSession2x;
    }

    private Bet getTheoreticalBet(Price price, int factor) {
        Bet bet = new Bet(price, INIT_BET * factor);
        bet.markPlaced();
        return bet;
    }

    private void printBettingSessionUpdate(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession, Bet bet) {
        String moreProfitableOutcomeId = bettingSession.getMoreProfitableOutcomeId();
        String lessProfitableOutcomeId = bettingSession.getLessProfitableOutcomeId();
        String moreProfitableOutcomeDescription = market.getOutcomes().get(moreProfitableOutcomeId).getDescription();
        String lessProfitableOutcomeDescription = market.getOutcomes().get(lessProfitableOutcomeId).getDescription();
        System.out.println(String.format("############################################\n" +
                "NEW BET:\n" +
                "\teventId: %d\n" +
                "\teventDescription: %s\n" +
                "\triskAmount: %.2f\n" +
                "\tmoneyLine: %s\n" +
                "\toutcome: %s\n" +
                "----------------\n"+
                "TOTALS:\n" +
                "\tminimumProfit (%s wins): %.2f\n"+
                "\tmaximumProfit (%s wins): %.2f\n"+
                "############################################",
                event.getId(),
                event.getDescription(),
                bet.getRiskAmount(),
                price.getAmerican() > 0 ? "+"+price.getAmerican() : price.getAmerican(),
                outcome.getDescription(),
                lessProfitableOutcomeDescription,
                bettingSession.getMinimumProfit(),
                moreProfitableOutcomeDescription,
                bettingSession.getMaximumProfit()));
    }

    public void updateBettingSession(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price, Strategy stategy) {
        switch (stategy) {
            case BASIC:
                updateBettingSessionBasic(event, market, outcome, opposingOutcome, price);
                break;
            default:
                // no implementation
        }
    }
}
