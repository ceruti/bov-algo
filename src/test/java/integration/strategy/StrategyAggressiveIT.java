package integration.strategy;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.model.Market;
import integration.StrategyAnalysisIntegrationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-aggressive"})
public class StrategyAggressiveIT extends StrategyAnalysisIntegrationTests {

    @Test
    public void testStrategyWorksAsExpected() throws Exception {
        Event eventSimulationResult = super._wireMessageReplay();
        Market moneyLineMarket = eventSimulationResult.getMarkets().values().stream()
                .filter(market -> market.getDescription().equalsIgnoreCase("Moneyline"))
                .findFirst().get();
        int totalBets = moneyLineMarket.getBettingSession().getPositions().values().stream()
                .reduce(0, (subtotal, position) -> subtotal + position.getBets().size(), Integer::sum);
        Assert.assertEquals(15, totalBets);
        Assert.assertEquals(14.55, moneyLineMarket.getExpectedProfit(), .01);
        Assert.assertEquals(6.25, moneyLineMarket.getMinimumProfit(), .01);
        Assert.assertEquals(15.25, moneyLineMarket.getMaximumProfit(), .01);
    }

    @Test
    public void runFullSimluation() throws Exception {
        super.testStrategy("AGGRESSIVE");
    }


}
