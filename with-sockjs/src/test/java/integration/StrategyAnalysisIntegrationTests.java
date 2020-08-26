package integration;

import lahsivjar.spring.websocket.template.Application;
import lahsivjar.spring.websocket.template.EventBook;
import lahsivjar.spring.websocket.template.EventRepository;
import lahsivjar.spring.websocket.template.EventSyncService;
import lahsivjar.spring.websocket.template.model.*;
import lahsivjar.spring.websocket.template.util.BettingFacilitatorService;
import lombok.AllArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    public void testBasicStrategy() {
        eventBook.setEnableUpdates(false);
        int moneyLines = 0;
        for (Event event : eventBook.getBook().values()) {
            Map<String, Market> markets = event.getMarkets();
            if (markets != null) {
                for (Market market : markets.values()) {
                    if (market.getDescription().equalsIgnoreCase("Moneyline")
                        || market.getDescription().equalsIgnoreCase("Fight Winner")) {
                        if (market.getOutcomes().size() == 2) {
                            List<Outcome> outcomeValues = new ArrayList<>(market.getOutcomes().values());
                            Outcome outcome1 = outcomeValues.get(0);
                            Outcome outcome2 = outcomeValues.get(1);
                            if (outcome1 != null && outcome2 != null) {
                                List<Price> previousPrices1 = outcome1.getPreviousPrices();
                                List<Price> previousPrices2 = outcome2.getPreviousPrices();
                                if (previousPrices1 != null
                                    && previousPrices2 != null
                                    && previousPrices1.size() > 10
                                    && previousPrices2.size() > 10) {
                                    logProfit(event, market, outcome1, outcome2, previousPrices1, previousPrices2);
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

    private void logProfit(Event event, Market market, Outcome outcome1, Outcome outcome2, List<Price> previousPrices1, List<Price> previousPrices2) {
        market.setBettingSession(null);
        List<OutcomeAndPrice> outcomeAndPrices = new ArrayList<>();
        for (Price price : previousPrices1) {
            outcomeAndPrices.add(new OutcomeAndPrice(outcome1, price));
        }
        for (Price price : previousPrices2) {
            outcomeAndPrices.add(new OutcomeAndPrice(outcome2, price));
        }
        outcomeAndPrices.sort((a, b) -> a.price.getCreated().compareTo(b.price.getCreated()));
        for (OutcomeAndPrice outcomeAndPrice : outcomeAndPrices) {
            Outcome oppposingOutcome = outcomeAndPrice.outcome.getId().equals(outcome1.getId()) ? outcome2 : outcome1;
            bettingFacilitatorService.updateBettingSession(event, market, outcomeAndPrice.outcome, oppposingOutcome, outcomeAndPrice.price, Strategy.BASIC);
        }
    }

    @AllArgsConstructor
    private class OutcomeAndPrice {
        Outcome outcome;
        Price price;
    }

}
