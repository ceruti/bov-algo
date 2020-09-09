package integration.strategy;

import integration.StrategyAnalysisIntegrationTests;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-account-for-minimum-profit"})
public class AccountForMinimumProfitIT extends StrategyAnalysisIntegrationTests {

    @Test
    public void runFullSimluation() throws Exception {
            super.testStrategy("ACCOUNT_FOR_MINIMUM_PROFIT_FULL_v2");
    }

    @Test
    public void testSingleEvent() throws Exception {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("_id").is(7671435L)
        );
        super.runSimulation("HIGH_SOFTENING", query);
    }


}
