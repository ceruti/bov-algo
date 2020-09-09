package integration.strategy;

import integration.StrategyAnalysisIntegrationTests;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-high_softening_allow_negative"})
public class StrategyHighSoftening_AllowNegativeIT extends StrategyAnalysisIntegrationTests {

    @Test
    public void runFullSimluation() throws Exception {
        super.testStrategy("HIGH_SOFTENING_ALLOW_NEGATIVE_INIT_50");
    }

    @Test
    public void testSingleEvent() throws Exception {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("_id").is(7671560L)
        );
        super.runSimulation("HIGH_SOFTENING_ALLOW_NEGATIVE_LAKERS_G2", query);
    }


}
