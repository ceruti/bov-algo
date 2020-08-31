package com.ceruti.bov.util;

import com.ceruti.bov.model.*;
import com.ceruti.bov.BetPlacingService;
import com.ceruti.bov.EventBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BettingFacilitatorService {

    private static final double INIT_BET = 5.00; // TODO: update amounts?
    public static final int LOWER_BOUND_MONEYLINE_ENTRY = 100;
    public static final int UPPERBOUND_MONEYLINE_ENTRY = 200;

    private EventBook eventBook;

    private BetPlacingService betPlacingService;

    private SimpMessagingTemplate template;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    public BettingFacilitatorService(BetPlacingService betPlacingService, SimpMessagingTemplate simpMessagingTemplate, EventBook eventBook) {
        this.betPlacingService = betPlacingService;
        this.template = simpMessagingTemplate;
        this.eventBook = eventBook;
    }

    private void updateBettingSessionBasic(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price) {
        if (event.hasAnotherBettingSession(market.getId())) {
            return;
        }
        BettingSession bettingSession = market.getBettingSession();
        if (bettingSession != null) {
            printBettingLineUpdate(event, outcome, price);
        }
        if (bettingSession == null && event.startedRecently() && !eventBook.isOnInitiateBettingSessionBlacklist(event) && price.getAmerican() > LOWER_BOUND_MONEYLINE_ENTRY && price.getAmerican() < UPPERBOUND_MONEYLINE_ENTRY) {
            attemptInitBettingSession(event, market, outcome, opposingOutcome, price);
        }
        else if (bettingSession != null && price.getAmerican() > LOWER_BOUND_MONEYLINE_ENTRY && price.getAmerican() < UPPERBOUND_MONEYLINE_ENTRY) {
            attemptPlaceAdditionalBet(event, market, outcome, price, bettingSession);
        }
    }

    private void printBettingLineUpdate(Event event, Outcome outcome, Price price) {
        System.out.println(String.format(
                "~~~~~~~~~~~~~~~~~~~~~\n"+
                "NEW LINE FOR ACTIVE BETTING SESSION:\n"+
                "\teventId: %d\n" +
                "\teventDescription: %s\n" +
                "\toutcome: %s\n"+
                "\tmoneyLine: %s\n" +
                "\ttime: %s\n"+
                "~~~~~~~~~~~~~~~~~~~~~\n",
                event.getId(),
                event.getDescription(),
                outcome.getDescription(),
                printAmericanPrice(price),
                price.getCreated().toString()
        ));
    }

    private void attemptPlaceAdditionalBet(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        BettingSession theoreticalBettingSession2x = getTheoreticalBettingSession(outcome, price, bettingSession, 2);
        BettingSession theoreticalBettingSession1x = getTheoreticalBettingSession(outcome, price, bettingSession, 1);
        if (bettingSession.getMinimumProfit() < INIT_BET) { // TODO: change this?
            // not making money yet -- we need to bet in the "opposite direction"
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

    public Market attemptPlaceCustomBet(Long eventId, String marketId, String outcomeId, String opposingOutcomeId, Price price, int amountInCents) {
        Bet bet = betPlacingService.placeBet(outcomeId, price, amountInCents);
        Market market = eventBook.getBook().get(eventId).getMarkets().get(marketId);
        if (market.getBettingSession() == null) {
            market.initBettingSession(bet, outcomeId, opposingOutcomeId);
        } else {
            market.updateBettingSession(bet, outcomeId);
        }
        return market;
    }

    private BettingSession attemptPlaceBetUpdate(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession, double riskAmount) {
        Bet bet = betPlacingService.placeBet(outcome.getId(), price, riskAmount);
        bettingSession.update(bet, outcome.getId());
        printBettingSessionUpdate(event, market, outcome, price, market.getBettingSession(), bet);
        template.convertAndSend( "/topics/all", event);
        return market.getBettingSession();
    }

    private BettingSession attemptInitBettingSession(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price) {
        Bet bet = betPlacingService.placeBet(outcome.getId(), price, INIT_BET);
        market.initBettingSession(bet, outcome.getId(), opposingOutcome.getId());
        printBettingSessionUpdate(event, market, outcome, price, market.getBettingSession(), bet);
        template.convertAndSend( "/topics/all", event);
        return market.getBettingSession();
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
        String betPlaced = String.format("############################################\n" +
                        "NEW BET:\n" +
                        "\teventId: %d\n" +
                        "\teventDescription: %s\n" +
                        "\triskAmount: %.2f\n" +
                        "\tmoneyLine: %s\n" +
                        "\toutcome: %s\n" +
                        "----------------\n" +
                        "TOTALS:\n" +
                        "\tminimumProfit (%s wins): %.2f\n" +
                        "\tmaximumProfit (%s wins): %.2f\n" +
                        "############################################",
                event.getId(),
                event.getDescription(),
                bet.getRiskAmount(),
                printAmericanPrice(price),
                outcome.getDescription(),
                lessProfitableOutcomeDescription,
                bettingSession.getMinimumProfit(),
                moreProfitableOutcomeDescription,
                bettingSession.getMaximumProfit());
        System.out.println(betPlaced);
//        this.template.convertAndSend("/topic/all", betPlaced);
    }

    private String printAmericanPrice(Price price) {
        return price.getAmerican() > 0 ? "+"+price.getAmerican() : Integer.toString(price.getAmerican());
    }

    public void updateBettingSessionAsync(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price, Strategy stategy) {
        executorService.submit(() -> {
           updateBettingSession(event, market, outcome, opposingOutcome, price, stategy);
        });
    }

    public void updateBettingSession(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price, Strategy stategy) {
        if (!event.isBettingEnabled() || !outcome.isBettingEnabled() || event.isEndingSoon()) {
            return;
        }
        switch (stategy) {
            case BASIC:
                updateBettingSessionBasic(event, market, outcome, opposingOutcome, price);
                break;
            default:
                // no implementation
        }
    }
}
