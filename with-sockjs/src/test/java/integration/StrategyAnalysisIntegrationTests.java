package integration;

import lahsivjar.spring.websocket.template.Application;
import lahsivjar.spring.websocket.template.EventBook;
import lahsivjar.spring.websocket.template.model.*;
import lahsivjar.spring.websocket.template.util.BettingFacilitatorService;
import lombok.AllArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class)
@AutoConfigureMockMvc
public class StrategyAnalysisIntegrationTests {

    public StrategyAnalysisIntegrationTests() {}

    @Autowired
    EventBook eventBook;

    @Autowired
    BettingFacilitatorService bettingFacilitatorService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    public void renameCollection() {
        eventBook.setEnableUpdates(false);
        mongoTemplate.getDb().getCollection("event").rename("eventDumpAug27");
    }


    @Test
    public void testBasicStrategy() {
        eventBook.setEnableUpdates(false);
        int moneyLines = 0;
        List<Event> events = mongoTemplate.findAll(Event.class, "event");
        for (Event event : events) {
            Map<String, Market> markets = event.getMarkets();
            if (markets != null) {
                for (Market market : markets.values()) {
                    if (market.getDescription().equalsIgnoreCase("Moneyline")) {
                        if (market.getOutcomes().size() == 2) {
                            List<Outcome> outcomeValues = new ArrayList<>(market.getOutcomes().values());
                            Outcome outcome1 = outcomeValues.get(0);
                            Outcome outcome2 = outcomeValues.get(1);
                            if (outcome1 != null && outcome2 != null) {
                                List<Price> previousPrices1 = outcome1.getPreviousPrices();
                                List<Price> previousPrices2 = outcome2.getPreviousPrices();
                                if (previousPrices1 != null
                                    && previousPrices2 != null
                                    && previousPrices1.size() > 15
                                    && previousPrices2.size() > 15) {
                                    simulateBettingStrategy(event, market, outcome1, outcome2, previousPrices1, previousPrices2);
                                    moneyLines++;
                                }
                            }

                        }
                    }
                }
            }
        }
        System.out.println("Moneyline markets: "+moneyLines);
    }

    private void simulateBettingStrategy(Event event, Market market, Outcome outcome1, Outcome outcome2, List<Price> previousPrices1, List<Price> previousPrices2) {
        List<OutcomeAndPriceTick> outcomeAndPriceTicks = getMergedPrices(outcome1, outcome2, previousPrices1, previousPrices2);
        String winningOutcomeId = market.getBettingSession() != null && market.getBettingSession().getWinningOutcomeId() != null ?
                market.getBettingSession().getWinningOutcomeId() : getWinningOutcome(market, outcomeAndPriceTicks);
        if (winningOutcomeId == null) {
            return;
        }
        market.setBettingSession(null);
        event.setBettingEnabled(true);
        BettingExecutionMetaResults bettingExecutionMetaResults = executeBettingStrategy(event, market, outcome1, outcome2, outcomeAndPriceTicks, winningOutcomeId);
        if (market.getBettingSession() == null) {
            // bet was never made
            return;
        }

        // TODO: do something with the executionResults (like write to DB)

        // AGGREGATE COLUMNS:
        // - average profit
        // - average profit by sport
        // - average favorite reversals
        // - average favorite reversals by sport

        // EVENT COLUMNS
        // - base obj columns: sport, eventId, winning outcome description
        // - num odds quotes
        // - profit
        // - num bets placed
        // - worst odds quote (for winner)
        // - best odds quote (for loser)
        // - num favorite reversals and:
        // - standard deviation for winner odds --> need to convert to fractional first
        // - standard deviation for loser odds --> need to convert to fractional first
        // - column for multiple betting strategies?? -> probably not

        // SOLUTION
        // how to accomplish:
        //      - write results of meta object to DB collection
        //      - make react table sortable by column
        //      - server-side queries on sort, or in-memory? server-side ideal, but try column first


    }

    private List<OutcomeAndPriceTick> getMergedPrices(Outcome outcome1, Outcome outcome2, List<Price> previousPrices1, List<Price> previousPrices2) {
        List<OutcomeAndPriceTick> outcomeAndPriceTicks = new ArrayList<>();
        for (Price price : previousPrices1) {
            outcomeAndPriceTicks.add(new OutcomeAndPriceTick(outcome1, price));
        }
        for (Price price : previousPrices2) {
            outcomeAndPriceTicks.add(new OutcomeAndPriceTick(outcome2, price));
        }
        outcomeAndPriceTicks.sort(Comparator.comparing(a -> a.price.getCreated()));
        return outcomeAndPriceTicks;
    }

    private BettingExecutionMetaResults executeBettingStrategy(Event event, Market market, Outcome outcome1, Outcome outcome2, List<OutcomeAndPriceTick> outcomeAndPriceTicks, String winningOutcomeId) {
        BettingExecutionMetaResults bettingExecutionMetaResults = new BettingExecutionMetaResults();
        boolean initialUnderdogSet = false;
        String lastFavoriteOutcomeId = outcomeAndPriceTicks.get(0).outcome.getId();
        for (int i = 0; i < outcomeAndPriceTicks.size(); i++) {
            OutcomeAndPriceTick currentTick = outcomeAndPriceTicks.get(i);
            if (currentTick.price.getAmerican() < 100
                    && !currentTick.outcome.getId().equals(lastFavoriteOutcomeId)) {
                bettingExecutionMetaResults.favoriteReversals++;
            }
            if (currentTick.outcome.getId().equals(winningOutcomeId)) {
                if (!initialUnderdogSet) {
                    bettingExecutionMetaResults.winnerWasInitialUnderdog = currentTick.price.getAmerican() > 0;
                    initialUnderdogSet = true;
                }
                if (currentTick.price.getAmerican() > bettingExecutionMetaResults.worstAmericanOddsForWinner) {
                    bettingExecutionMetaResults.worstAmericanOddsForWinner = currentTick.price.getAmerican();
                }
            }
            Outcome opposingOutcome = currentTick.outcome.getId().equals(outcome1.getId()) ? outcome2 : outcome1;
            bettingFacilitatorService.updateBettingSession(event, market, currentTick.outcome, opposingOutcome, currentTick.price, Strategy.BASIC);
            lastFavoriteOutcomeId = currentTick.outcome.getId();
        }
        if (market.getBettingSession() != null) {
            market.getBettingSession().setWinningOutcomeId(winningOutcomeId);
        }
        return bettingExecutionMetaResults;
    }

    private static class BettingExecutionMetaResults {
        boolean winnerWasInitialUnderdog;
        private int favoriteReversals;
        private int worstAmericanOddsForWinner = -1000000;
    }

    private String getWinningOutcome(Market market, List<OutcomeAndPriceTick> outcomeAndPriceTicks) {
        for (int i = outcomeAndPriceTicks.size()-1; i >= 0; i--) {
            OutcomeAndPriceTick lastOutcomeAndPriceTick = outcomeAndPriceTicks.get(i);
            if (lastOutcomeAndPriceTick.price.getAmerican() < 0) {
                return lastOutcomeAndPriceTick.outcome.getId();
            }
        }
        return null;
    }

    @AllArgsConstructor
    private class OutcomeAndPriceTick {
        Outcome outcome;
        Price price;
    }

}
