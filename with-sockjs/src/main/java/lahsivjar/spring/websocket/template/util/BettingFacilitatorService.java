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
            Bet bet = betPlacingService.placeBet(outcome, price, INIT_BET);
            market.initBettingSession(bet, outcome.getId(), opposingOutcome.getId());
            printInitBettingSession(event, outcome, price);
        }
        else {
            // TODO: implement

        }
    }

    private void printInitBettingSession(Event event, Outcome outcome, Price price) {
        System.out.println(String.format("############################################\n" +
                "NEW BETTING SESSION:\n" +
                "\teventId: %d\n" +
                "\teventDescription: %s\n" +
                "\tmoneyLine: %s\n" +
                "\toutcome: %s\n" +
                "############################################",
                event.getId(),
                event.getDescription(),
                price.getAmerican() > 0 ? "+"+price.getAmerican() : price.getAmerican(),
                outcome.getDescription()));
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
