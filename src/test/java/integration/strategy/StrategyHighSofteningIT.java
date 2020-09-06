package integration.strategy;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.model.Market;
import integration.StrategyAnalysisIntegrationTests;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy-high_softening"})
public class StrategyHighSofteningIT extends StrategyAnalysisIntegrationTests {

    @Test
    public void runFullSimluation() throws Exception {
        super.testStrategy("HIGH_SOFTENING_FULL");
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
