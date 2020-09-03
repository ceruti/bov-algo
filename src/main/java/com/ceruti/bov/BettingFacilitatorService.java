package com.ceruti.bov;

import com.ceruti.bov.model.*;
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
    private final SharedExecutorService sharedExecutorService;
    private ExecutorService customBetPlacingExecutorService;

    private EventBook eventBook;

    private BetPlacingService betPlacingService;

    private SimpMessagingTemplate template;

    @Autowired
    public BettingFacilitatorService(BetPlacingService betPlacingService, SimpMessagingTemplate simpMessagingTemplate, EventBook eventBook, SharedExecutorService sharedExecutorService) {
        this.betPlacingService = betPlacingService;
        this.template = simpMessagingTemplate;
        this.eventBook = eventBook;
        this.sharedExecutorService = sharedExecutorService;
        this.customBetPlacingExecutorService = Executors.newFixedThreadPool(2);
    }

    private void printBettingLineUpdate(Event event, Outcome outcome, Price price) {
//        System.out.println(String.format(
//                "~~~~~~~~~~~~~~~~~~~~~\n"+
//                "NEW LINE FOR ACTIVE BETTING SESSION:\n"+
//                "\teventId: %d\n" +
//                "\teventDescription: %s\n" +
//                "\toutcome: %s\n"+
//                "\tmoneyLine: %s\n" +
//                "\ttime: %s\n"+
//                "~~~~~~~~~~~~~~~~~~~~~\n",
//                event.getId(),
//                event.getDescription(),
//                outcome.getDescription(),
//                printAmericanPrice(price),
//                price.getCreated().toString()
//        ));
    }

    private void attemptPlaceAdditionalBetBasic(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        BettingSession theoreticalBettingSession2x = getTheoreticalBettingSession(outcome, price, bettingSession, 2);
        BettingSession theoreticalBettingSession1x = getTheoreticalBettingSession(outcome, price, bettingSession, 1);
        double currentMinimumProfit = bettingSession.getMinimumProfit();
        double theoreticalBettingSession1xMinimumProfit = theoreticalBettingSession1x.getMinimumProfit();
        double theoreticalBettingSession2xMinimumProfit = theoreticalBettingSession2x.getMinimumProfit();
        if (currentMinimumProfit < INIT_BET) { // TODO: change this?
            // not making money yet -- we need to bet in the "opposite direction"
            if (theoreticalBettingSession2xMinimumProfit >= currentMinimumProfit) {
                attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, INIT_BET * 2);
            } else if (theoreticalBettingSession1xMinimumProfit >= currentMinimumProfit) {
                attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, INIT_BET);
            }
       } else {
            // we've already profited
            // so we can afford some risk again, hoping it will keep swinging back and forth
            attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, INIT_BET);
       }
    }

    private void attemptPlaceAdditionalBetAggressive(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        attemptPlaceAdditionalBet(event, market, outcome, price, bettingSession, INIT_BET, 0);
    }

    private void attemptPlaceAdditionalBet(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession, double currentMinimumProfitThreshold, double holdMinimumProfitsAbove) {
        BettingSession theoreticalBettingSession2x = getTheoreticalBettingSession(outcome, price, bettingSession, 2);
        BettingSession theoreticalBettingSession1x = getTheoreticalBettingSession(outcome, price, bettingSession, 1);
        double currentMinimumProfit = bettingSession.getMinimumProfit();
        double theoreticalBettingSession1xMinimumProfit = theoreticalBettingSession1x.getMinimumProfit();
        double theoreticalBettingSession2xMinimumProfit = theoreticalBettingSession2x.getMinimumProfit();
        if (currentMinimumProfit < currentMinimumProfitThreshold) { // TODO: change this?
            // not making money yet -- we need to bet in the "opposite direction"
            if (theoreticalBettingSession2xMinimumProfit >= holdMinimumProfitsAbove || theoreticalBettingSession2xMinimumProfit >= currentMinimumProfit) {
                attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, INIT_BET * 2);
            } else if (theoreticalBettingSession1xMinimumProfit >= holdMinimumProfitsAbove || theoreticalBettingSession1xMinimumProfit >= currentMinimumProfit) {
                attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, INIT_BET);
            }
        } else {
            // we've already profited
            // so we can afford some risk again, hoping it will keep swinging back and forth
            attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, INIT_BET);
        }
    }

    private void attemptPlaceAdditionalBetRetainProfits(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession) {
        attemptPlaceAdditionalBet(event, market, outcome, price, bettingSession, INIT_BET*2, INIT_BET);
    }

    public void attemptPlaceCustomBetAsync(Long eventId, String marketId, String outcomeId, String opposingOutcomeId, Price price, int amountInCents) {
        customBetPlacingExecutorService.submit(() -> {
            attemptPlaceCustomBet(eventId, marketId, outcomeId, opposingOutcomeId, price, amountInCents);
        });
    }

    public void attemptPlaceCustomBet(Long eventId, String marketId, String outcomeId, String opposingOutcomeId, Price price, int amountInCents) {
        Event event = eventBook.getBook().get(eventId);
        Market market = event.getMarkets().get(marketId);
        Outcome outcome = market.getOutcomes().values().stream()
                .filter(_outcome -> _outcome.getId().equalsIgnoreCase(outcomeId))
                .findFirst().get();
        Outcome opposingOutcome = market.getOutcomes().values().stream()
                .filter(_outcome -> _outcome.getId().equalsIgnoreCase(opposingOutcomeId))
                .findFirst().get();
        BettingSession bettingSession = market.getBettingSession();
        if (bettingSession == null) {
            attemptInitBettingSession(event, market, outcome, opposingOutcome, price);
        } else {
            attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, amountInCents / 100.0);
        }
    }

    private BettingSession attemptPlaceBetUpdate(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession, double riskAmount) {
        int amountInCents = toAmountInCents(riskAmount);
        Bet bet = betPlacingService.initBet(price, amountInCents);
        bettingSession.update(bet, outcome.getId());
        return submitBet(event, market, outcome, price, amountInCents, bet);
    }

    private int toAmountInCents(double riskAmount) {
        return (int) (Math.ceil(riskAmount * 100));
    }

    private BettingSession attemptInitBettingSession(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price) {
        int amountInCents = toAmountInCents(INIT_BET);
        Bet bet = betPlacingService.initBet(price, amountInCents);
        market.initBettingSession(bet, outcome.getId(), opposingOutcome.getId());
        return submitBet(event, market, outcome, price, amountInCents, bet);
    }

    private BettingSession submitBet(Event event, Market market, Outcome outcome, Price price, int amountInCents, Bet bet) {
        template.convertAndSend("/topics/all", event); // broadcast that bet is in progress
        betPlacingService.submitBet(outcome.getId(), price, amountInCents, bet);
        template.convertAndSend("/topics/all", event); // broadcast that bet is placed
        printBettingSessionUpdate(event, market, outcome, price, market.getBettingSession(), bet);
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
        String opposingOutcomeId = outcome.getId().equals(moreProfitableOutcomeId) ? lessProfitableOutcomeId : moreProfitableOutcomeId;
        String moreProfitableOutcomeDescription = market.getOutcomes().get(moreProfitableOutcomeId).getDescription();
        String lessProfitableOutcomeDescription = market.getOutcomes().get(lessProfitableOutcomeId).getDescription();
        Price opposingPrice = market.getOutcomes().get(opposingOutcomeId).getPrice();
        String opposingPriceId = opposingPrice == null ? null : opposingPrice.getId();
        String betPlaced = String.format("############################################\n" +
                        "NEW BET:\n" +
                        "\teventId: %d\n" +
                        "\teventDescription: %s\n" +
                        "\triskAmount: %.2f\n" +
                        "\tmoneyLine: %s\n" +
                        "\toutcome: %s\n" +
                        "----------------\n" +
                        "\toutcomePriceId: %s\n" +
                        "\topposingOutcomePriceId: %s\n" +
                        "----------------\n" +
                        "TOTALS:\n" +
                        "\tminimumProfit (%s wins): %.2f\n" +
                        "\tmaximumProfit (%s wins): %.2f\n" +
                        "\texpectedProfit: %.2f\n" +
                        "############################################",
                event.getId(),
                event.getDescription(),
                bet.getRiskAmount(),
                printAmericanPrice(price),
                outcome.getDescription(),
                outcome.getPrice().getId(),
                opposingPriceId,
                lessProfitableOutcomeDescription,
                bettingSession.getMinimumProfit(),
                moreProfitableOutcomeDescription,
                bettingSession.getMaximumProfit(),
                market.getExpectedProfit());
        System.out.println(betPlaced);
        System.out.println(""); // TODO: delete
//        this.template.convertAndSend("/topic/all", betPlaced);
    }

    private String printAmericanPrice(Price price) {
        return price.getAmerican() > 0 ? "+"+price.getAmerican() : Integer.toString(price.getAmerican());
    }

    public void updateBettingSessionAsync(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price, Strategy stategy) {
        sharedExecutorService.getExecutorService().submit(() -> {
           updateBettingSession(event, market, outcome, opposingOutcome, price, stategy);
        });
    }

    public void updateBettingSession(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price, Strategy strategy) {
        if (!event.isBettingEnabled() || !outcome.isBettingEnabled() || event.isEndingSoon()) {
            return;
        }
        synchronized (market) { // don't want multiple threads making bets on the same thing
            if (event.hasAnotherBettingSession(market.getId())) {
                return;
            }
            BettingSession bettingSession = market.getBettingSession();
            if (bettingSession != null) {
                printBettingLineUpdate(event, outcome, price);
            }
            if (bettingSession == null /*&& event.startedRecently()*/ && !eventBook.isOnInitiateBettingSessionBlacklist(event) && price.getAmerican() > LOWER_BOUND_MONEYLINE_ENTRY && price.getAmerican() < UPPERBOUND_MONEYLINE_ENTRY) {
                attemptInitBettingSession(event, market, outcome, opposingOutcome, price);
            }
            else if (bettingSession != null && price.getAmerican() > LOWER_BOUND_MONEYLINE_ENTRY && price.getAmerican() < UPPERBOUND_MONEYLINE_ENTRY) {
                switch (strategy) {
                    case BASIC:
                        attemptPlaceAdditionalBetBasic(event, market, outcome, price, bettingSession);
                        break;
                    case AGGRESSIVE:
                        attemptPlaceAdditionalBetAggressive(event, market, outcome, price, bettingSession);
                        break;
                    case RETAIN_PROFITS:
                        attemptPlaceAdditionalBetRetainProfits(event, market, outcome, price, bettingSession);
                        break;
                }
            }
        }
    }
}
