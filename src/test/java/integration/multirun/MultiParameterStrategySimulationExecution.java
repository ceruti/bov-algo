package integration.multirun;

import com.ceruti.bov.BettingFacilitatorService;
import com.ceruti.bov.model.Event;
import integration.StrategyAnalysisIntegrationTests;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles({"test", "no-logs"}) // TODO: turn off logs when this profile active
public abstract class MultiParameterStrategySimulationExecution extends StrategyAnalysisIntegrationTests {

    @Autowired
    BettingFacilitatorService bettingFacilitatorService;

    @Autowired
    MongoTemplate mongoTemplate;

    private static List<Event> events;

    private static boolean init = false;

    protected abstract String getStrategyName();

    @Before // NOTE: Junit 5 @BeforeAll would work better, but it's not compatible with @SpringBootTest
    public void beforeAll() {
        if (!init) {
            System.out.println("Loading all events into memory (this will take a while)...");
            Query query = new Query();
            String[] allowedVersions = new String[]{"v2.4"};
            query.addCriteria(Criteria.where("version").in(allowedVersions));
            query.limit(10000); // TODO: uncomment
            events = mongoTemplate.find(query, Event.class, "event");
            System.out.println("Done loading events into memory!");
            init = true;
        }
    }

    @Test
    public void ib_20__ff_true() throws Exception {
        bettingFacilitatorService.setFavoriteFirst(true);
        BettingFacilitatorService.INIT_BET = 20;
        super.runSimulation(getStrategyName()+ "__IB-20"+"__FF-TRUE", events);
    }

    @Test
    public void ib_50__ff_true() throws Exception {
        bettingFacilitatorService.setFavoriteFirst(true);
        BettingFacilitatorService.INIT_BET = 50;
        super.runSimulation(getStrategyName()+ "__IB-50"+"__FF-TRUE", events);
    }

    @Test
    public void ib_100__ff_true() throws Exception {
        bettingFacilitatorService.setFavoriteFirst(true);
        BettingFacilitatorService.INIT_BET = 100;
        super.runSimulation(getStrategyName()+ "__IB-100"+"__FF-TRUE", events);
    }

    @Test
    public void ib_20__ff_false() throws Exception {
        bettingFacilitatorService.setFavoriteFirst(false);
        BettingFacilitatorService.INIT_BET = 20;
        super.runSimulation(getStrategyName()+ "__IB-20"+"__FF-FALSE", events);
    }

    @Test
    public void ib_50__ff_false() throws Exception {
        bettingFacilitatorService.setFavoriteFirst(false);
        BettingFacilitatorService.INIT_BET = 50;
        super.runSimulation(getStrategyName()+ "__IB-50"+"__FF-FALSE", events);
    }

    @Test
    public void ib_100__ff_false() throws Exception {
        bettingFacilitatorService.setFavoriteFirst(false);
        BettingFacilitatorService.INIT_BET = 100;
        super.runSimulation(getStrategyName()+ "__IB-100"+"__FF-FALSE", events);
    }

}
