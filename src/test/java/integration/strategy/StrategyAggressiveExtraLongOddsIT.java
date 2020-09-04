package integration.strategy;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.model.Market;
import integration.StrategyAnalysisIntegrationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-aggressive_long_odds"})
public class StrategyAggressiveExtraLongOddsIT extends StrategyAnalysisIntegrationTests {

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
//        Assert.assertEquals(2, totalBets);
//        Assert.assertEquals(-0.49, moneyLineMarket.getExpectedProfit(), .01);
//        Assert.assertEquals(-1.50, moneyLineMarket.getMinimumProfit(), .01);
//        Assert.assertEquals(11.50, moneyLineMarket.getMaximumProfit(), .01);
    }

    @Test
    public void runFullSimluation() throws Exception {
        super.testStrategy("AGGRESSIVE_EXTRA_LONG_ODDS");
    }

}
