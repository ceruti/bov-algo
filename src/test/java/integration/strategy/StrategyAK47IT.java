package integration.strategy;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.model.Market;
import integration.StrategyAnalysisIntegrationTests;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-ak47"})
public class StrategyAK47IT extends StrategyAnalysisIntegrationTests {

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
        super.testStrategy("AK47");
    }

    @Test
    public void testSingleEvent() throws Exception {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("_id").is(7671435L)
        );
        super.runSimulation("AK47", query);
    }


}
