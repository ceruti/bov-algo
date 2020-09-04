package integration.strategy;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.model.Market;
import integration.StrategyAnalysisIntegrationTests;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-variable_amount_favor_long_high_threshold"})
public class StrategyVariableAmounts_FavorLongOdds_HighThreshold_IT extends StrategyAnalysisIntegrationTests {

    @Test
    public void testStrategyWorksAsExpected() throws Exception {
        Event eventSimulationResult = super._wireMessageReplay();
        Market moneyLineMarket = eventSimulationResult.getMarkets().values().stream()
                .filter(market -> market.getDescription().equalsIgnoreCase("Moneyline"))
                .findFirst().get();
        int totalBets = moneyLineMarket.getBettingSession().getPositions().values().stream()
                .reduce(0, (subtotal, position) -> subtotal + position.getBets().size(), Integer::sum);
        System.out.println("Total bets: "+totalBets);
        System.out.println("Expected profit: "+moneyLineMarket.getExpectedProfit());
        System.out.println("Minimum profit: "+moneyLineMarket.getMinimumProfit());
        System.out.println("Maximum profit: "+moneyLineMarket.getMaximumProfit());
//        Assert.assertEquals(24, totalBets);
//        Assert.assertEquals(2.06, moneyLineMarket.getExpectedProfit(), .01);
//        Assert.assertEquals(0.78, moneyLineMarket.getMinimumProfit(), .01);
//        Assert.assertEquals(17.28, moneyLineMarket.getMaximumProfit(), .01);
    }

    @Test
    public void runFullSimluation() throws Exception {
        super.testStrategy("VARIABLE_FAVOR_LONG_ODDS_HIGH_THRESHOLD");
    }

}
