package com.ceruti.bov;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.BettingStrategyService;
import com.ceruti.bov.util.ActiveProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BettingFacilitatorService {

    public static int FAVORITE_INITIAL_BET_THRESHOLD = -200;
    public static int UNDERDOG_INITIAL_BET_THRESHOLD = 200;
    public static double INIT_BET = 20.00; // TODO: update amounts?
    public static final double VENDOR_MINIMUM_BET_AMOUNT = 0.50;
    private boolean favoriteFirst = false;

    private SharedExecutorService sharedExecutorService;
    private BettingStrategyService bettingStrategyService;
    private ExecutorService customBetPlacingExecutorService;
    private EventBook eventBook;
    private BetPlacingService betPlacingService;
    private SimpMessagingTemplate template;
    private ActiveProfileService activeProfileService;

    @Autowired
    public BettingFacilitatorService(BetPlacingService betPlacingService,
                                     SimpMessagingTemplate simpMessagingTemplate,
                                     EventBook eventBook,
                                     SharedExecutorService sharedExecutorService,
                                     BettingStrategyService bettingStrategyService,
                                     ActiveProfileService activeProfileService) {
        this.betPlacingService = betPlacingService;
        this.template = simpMessagingTemplate;
        this.eventBook = eventBook;
        this.sharedExecutorService = sharedExecutorService;
        this.customBetPlacingExecutorService = Executors.newFixedThreadPool(2);
        this.bettingStrategyService = bettingStrategyService;
        this.activeProfileService = activeProfileService;
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
            attemptInitBettingSession(event, market, outcome, opposingOutcome, price, amountInCents);
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

    private BettingSession attemptInitBettingSession(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price, int amountInCents) {
        Bet bet = betPlacingService.initBet(price, amountInCents);
        market.initBettingSession(bet, outcome.getId(), opposingOutcome.getId());
        return submitBet(event, market, outcome, price, amountInCents, bet);
    }

    private BettingSession attemptInitBettingSession(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price) {
        return attemptInitBettingSession(event, market, outcome, opposingOutcome, price, toAmountInCents(INIT_BET));
    }

    private BettingSession submitBet(Event event, Market market, Outcome outcome, Price price, int amountInCents, Bet bet) {
        template.convertAndSend("/topic/all", event); // broadcast that bet is in progress
        betPlacingService.submitBet(outcome.getId(), price, amountInCents, bet);
        template.convertAndSend("/topic/all", event); // broadcast that bet is placed
        printBettingSessionUpdate(event, market, outcome, price, market.getBettingSession(), bet);
        return market.getBettingSession();
    }

    private void printBettingSessionUpdate(Event event, Market market, Outcome outcome, Price price, BettingSession bettingSession, Bet bet) {
        String moreProfitableOutcomeId = bettingSession.getMoreProfitableOutcomeId();
        String lessProfitableOutcomeId = bettingSession.getLessProfitableOutcomeId();
        String opposingOutcomeId = outcome.getId().equals(moreProfitableOutcomeId) ? lessProfitableOutcomeId : moreProfitableOutcomeId;
        String moreProfitableOutcomeDescription = market.getOutcomes().get(moreProfitableOutcomeId).getDescription();
        String lessProfitableOutcomeDescription = market.getOutcomes().get(lessProfitableOutcomeId).getDescription();
        Price opposingPrice = market.getOutcomes().get(opposingOutcomeId).getPrice();
        String opposingPriceId = opposingPrice == null ? null : opposingPrice.getId();
        String period = event.getClock() != null ? event.getClock().getPeriod() : "";
        String gameTime = event.getClock() != null ? event.getClock().getGameTime() : "";
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
                        "\tHome Score: %s\n" +
                        "\tVisitor Score: %s\n" +
                        "\tHome Period Score: %d\n"+
                        "\tVisitor Period Score %d\n"+
                        "\tPeriod: %s\n"+
                        "\tGame Time: %s\n"+
                        "----------------\n"+
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
                event.getHomeScore(),
                event.getVisitorScore(),
                event.getCurrentPeriodHomeScore(),
                event.getCurrentPeriodVisitorScore(),
                period,
                gameTime,
                lessProfitableOutcomeDescription,
                bettingSession.getMinimumProfit(),
                moreProfitableOutcomeDescription,
                bettingSession.getMaximumProfit(),
                market.getExpectedProfit());
        System.out.println(betPlaced);
    }

    private String printAmericanPrice(Price price) {
        return price.getAmerican() > 0 ? "+"+price.getAmerican() : Integer.toString(price.getAmerican());
    }


    public void updateBettingSession(Event event, Market market, Outcome outcome, Outcome opposingOutcome, Price price) {
        if (!event.isBettingEnabled() || !outcome.isBettingEnabled() || endingSoon(event)) {
            return;
        }
        synchronized (market) { // don't want multiple threads making bets on the same thing
            if (event.hasAnotherBettingSession(market.getId())) {
                return;
            }
            BettingSession bettingSession = market.getBettingSession();
            // TODO: figure out startedRecently() flag behavior
            if (bettingSession == null /*&& event.startedRecently()*/
                    && (outcome.isForceBettingEnabled() ||
                        (!eventBook.isOnInitiateBettingSessionBlacklist(event) && withinInitBetThreshold(price))
                    )
            ) {
                attemptInitBettingSession(event, market, outcome, opposingOutcome, price);
            }
            else if (bettingSession != null && (bettingStrategyService.isWithinBoundariesForAdditionalBet(price.getAmerican()) || outcome.isForceBettingEnabled())) {
                double additionalBetRiskAmount = bettingStrategyService.getAdditionalBetRiskAmount(event, market, outcome, price, bettingSession);
                if (additionalBetRiskAmount > VENDOR_MINIMUM_BET_AMOUNT) {
                    attemptPlaceBetUpdate(event, market, outcome, price, bettingSession, additionalBetRiskAmount);
                }
            }
        }
    }

    private boolean withinInitBetThreshold(Price price) {
        return favoriteFirst() ? withinFavoriteInitBetThreshold(price) : withinUnderdogInitBetThreshold(price);
    }

    private boolean favoriteFirst() {
        return activeProfileService.profileIsActive("favorite_first") || favoriteFirst;
    }

    private boolean withinUnderdogInitBetThreshold(Price price) {
        return price.getAmerican() > 0 && price.getAmerican() < UNDERDOG_INITIAL_BET_THRESHOLD;
    }

    private boolean withinFavoriteInitBetThreshold(Price price) {
        return price.getAmerican() < 0 && price.getAmerican() >= FAVORITE_INITIAL_BET_THRESHOLD;
    }

    protected boolean endingSoon(Event event) { // only turn off auto-betting in simulation mode. For live betting... just pay attention to the game clock
        return activeProfileService.isTestMode() && event.isEndingSoon();
    }

    public void setFavoriteFirst(boolean favoriteFirst) {
        this.favoriteFirst = favoriteFirst;
    }

}
