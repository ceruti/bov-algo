package integration.strategy;

import integration.StrategyAnalysisIntegrationTests;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy_smart-profit-differential"})
public class StrategySmartProfitDifferentialIT extends StrategyAnalysisIntegrationTests {

    @Test
    public void runFullSimluation() throws Exception {
        super.testStrategy("SMART_PROFIT_DIFFERENTIAL_UNDERDOG_FIRST");
    }

    @Test
    public void testFavoriteDominates_noOddsSwings() throws Exception {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("_id").is(7675249L)
        );
        super.runSimulation("SMART_PROFIT_DIFFERENTIAL_SINGLE", query);
    }

    @Test
    public void testFavoriteLoses_withOddsSwings() throws Exception {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("_id").is(7690359L)
        );
        super.runSimulation("SMART_PROFIT_DIFFERENTIAL_SINGLE", query);
    }


}
