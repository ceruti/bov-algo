package integration.strategy;

import integration.StrategyAnalysisIntegrationTests;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-high_softening_high_threshold"})
public class StrategyHighSofteningHighThresholdIT extends StrategyAnalysisIntegrationTests {

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
        super.runSimulation("HIGH_SOFTENING_HIGH_THRESHOLD", query);
    }


}
