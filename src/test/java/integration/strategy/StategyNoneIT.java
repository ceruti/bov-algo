package integration.strategy;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.model.Market;
import com.fasterxml.jackson.core.JsonProcessingException;
import integration.StrategyAnalysisIntegrationTests;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-none"})
public class StategyNoneIT extends StrategyAnalysisIntegrationTests {

    @Test
    public void testStrategyWorksAsExpected() throws Exception {
        Event eventSimulationResult = super._wireMessageReplay();
        Market moneyLineMarket = eventSimulationResult.getMarkets().values().stream()
                .filter(market -> market.getDescription().equalsIgnoreCase("Moneyline"))
                .findFirst().get();
        int totalBets = moneyLineMarket.getBettingSession().getPositions().values().stream()
                .reduce(0, (subtotal, position) -> subtotal + position.getBets().size(), Integer::sum);
        Assert.assertEquals(1, totalBets);
    }

}
