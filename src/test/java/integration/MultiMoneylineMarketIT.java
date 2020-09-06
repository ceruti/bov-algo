package integration;

import com.ceruti.bov.Application;
import com.ceruti.bov.NullAwareBeanUtilsBean;
import com.ceruti.bov.model.Event;
import com.ceruti.bov.model.Market;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.InvocationTargetException;

/**
 * This class is intended to simulate the behavior when multiple moneyline markets exist for an event.
 *
 * This is common for Bovada when a moneyline market will be offered before the event begins, but will use a different moneyline market once the event begins for live betting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class)
@ActiveProfiles({"test", "strategy-none"})
@AutoConfigureMockMvc
public class MultiMoneylineMarketIT {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    NullAwareBeanUtilsBean nullAwareBeanUtilsBean;

    @Test
    public void testMoneylineSwitchover() throws Exception {
        // when a new moneyline market is discovered
        // AND description = "Moneyline" AND descriptionKey = "Head to Head"
        // AND there are no previousPrices on current moneyLine market
        // THEN
        // * copy the bettingSession to the new market
        // * disable auto-betting on the old outcomes
        // * Rename old market to "(NON-ACTIVE): Moneyline"
        // * assert bets are made on new market

        // to test:
        // let toUpdate = event with 3 markets & betting session
        // let source = event with 6 markets (no betting session on new)
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(123456L));
        Event toUpdate = mongoTemplate.findOne(query, Event.class, "event");
        toUpdate.getMarkets().remove("126890422");
        Event source = mongoTemplate.findOne(query, Event.class, "event");

        nullAwareBeanUtilsBean.updateEvent(toUpdate, source);
        Market inactiveMarket = toUpdate.getMarkets().get("126717393");
        Assert.assertNull(inactiveMarket.getBettingSession());
        Assert.assertEquals("(INACTIVE) Moneyline", inactiveMarket.getDescription());
        Market activeMarket = toUpdate.getMarkets().get("126890422");
        Assert.assertNotNull(activeMarket.getBettingSession());
    }

}
