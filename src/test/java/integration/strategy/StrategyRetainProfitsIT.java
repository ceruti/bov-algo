package integration.strategy;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.model.Market;
import integration.StrategyAnalysisIntegrationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-retain_profits"})
public class StrategyRetainProfitsIT extends StrategyAnalysisIntegrationTests {

    @Test
    public void testStrategyWorksAsExpected() throws Exception {
        Event eventSimulationResult = super._wireMessageReplay();
        Market moneyLineMarket = eventSimulationResult.getMarkets().values().stream()
                .filter(market -> market.getDescription().equalsIgnoreCase("Moneyline"))
                .findFirst().get();
        int totalBets = moneyLineMarket.getBettingSession().getPositions().values().stream()
                .reduce(0, (subtotal, position) -> subtotal + position.getBets().size(), Integer::sum);
        System.out.println(totalBets);
        System.out.println(moneyLineMarket.getExpectedProfit());
        System.out.println(moneyLineMarket.getMinimumProfit());
        System.out.println(moneyLineMarket.getMaximumProfit());
        Assert.assertEquals(4, totalBets);
        Assert.assertEquals(4.69, moneyLineMarket.getExpectedProfit(), .01);
        Assert.assertEquals(4.50, moneyLineMarket.getMinimumProfit(), .01);
        Assert.assertEquals(7.00, moneyLineMarket.getMaximumProfit(), .01);
    }

}
