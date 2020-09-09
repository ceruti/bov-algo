package integration.strategy;

import integration.StrategyAnalysisIntegrationTests;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-high_softening_geometric_v2"})
public class StrategyHighSofteningGeometricIT extends StrategyAnalysisIntegrationTests {

    @Test
    public void runFullSimluation() throws Exception {
            super.testStrategy("HIGH_SOFTENING_GEOMETRIC_V2_FULL_50_INIT");
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
