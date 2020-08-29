package integration;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import lahsivjar.spring.websocket.template.Application;
import lahsivjar.spring.websocket.template.BettingExecutionMetaResultsRepository;
import lahsivjar.spring.websocket.template.EventBook;
import lahsivjar.spring.websocket.template.model.*;
import lahsivjar.spring.websocket.template.util.BettingFacilitatorService;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import static com.mongodb.client.model.Filters.exists;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class)
@AutoConfigureMockMvc
public class StrategyAnalysisIntegrationTests {

    public StrategyAnalysisIntegrationTests() {}

    @Autowired
    EventBook eventBook;

    @Autowired
    BettingFacilitatorService bettingFacilitatorService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    MongoClient mongoClient;

    @Autowired
    BettingExecutionMetaResultsRepository analysisRepository;

    @Test
//    @Ignore
    public void renameCollection() {
        eventBook.setEnableUpdates(false);
        mongoTemplate.getDb().getCollection("bettingExecutionMetaResults-1598637977138").rename("meta");
    }

    @Test
//    @Ignore
    public void testBasicStrategy() throws Exception {
        eventBook.setEnableUpdates(false);
        List<Event> events = mongoTemplate.findAll(Event.class, "event");
        List<BettingExecutionMetaResults> bettingExecutionMetaResultsBuffer = new ArrayList<>();
        for (Event event : events) {
            event.setSport(EventBook.getEquivalentKey(event.getSport()));
            Map<String, Market> markets = event.getMarkets();
            if (markets != null) {
                for (Market market : markets.values()) {
                    if (market.getDescription().equalsIgnoreCase("Moneyline")) {
                        if (market.getOutcomes().size() == 2) {
                            List<Outcome> outcomeValues = new ArrayList<>(market.getOutcomes().values());
                            Outcome outcome1 = outcomeValues.get(0);
                            Outcome outcome2 = outcomeValues.get(1);
                            if (outcome1 != null && outcome2 != null) {
                                List<Price> previousPrices1 = outcome1.getPreviousPrices();
                                List<Price> previousPrices2 = outcome2.getPreviousPrices();
                                if (previousPrices1 != null
                                    && previousPrices2 != null
                                    && previousPrices1.size() > 15
                                    && previousPrices2.size() > 15) {
                                    BettingExecutionMetaResults bettingExecutionMetaResults = simulateBettingStrategy(event, market, outcome1, outcome2, previousPrices1, previousPrices2);
                                    if (bettingExecutionMetaResults != null) {
                                        bettingExecutionMetaResultsBuffer.add(bettingExecutionMetaResults);
                                    }
                                    if (bettingExecutionMetaResultsBuffer.size() > 100) {
                                        analysisRepository.save(bettingExecutionMetaResultsBuffer);
                                        bettingExecutionMetaResultsBuffer = new ArrayList<>();
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
        String collectionName = "bettingExecutionMetaResults-" + new DateTime().getMillis();
        mongoTemplate.getCollection("bettingExecutionMetaResults").rename(collectionName);
        SimulationAggregateResult simulationAggregateResult = computeAggregation(collectionName);
        mongoTemplate.save(simulationAggregateResult, "simulationAggregations");
    }

    @Test
    public void testComputeAggregation() throws Exception {
        computeAggregation("meta");
    }

    private SimulationAggregateResult computeAggregation(String collectionName) throws Exception {
        SimulationAggregateResult simulationAggregateResult = new SimulationAggregateResult();
        simulationAggregateResult.setResults(new HashMap<>());
        simulationAggregateResult.setId(collectionName);
        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> collection = database.getCollection(collectionName);
        MongoCollection<Document> dbCollection = mongoClient.getDatabase("test").getCollection(collectionName, Document.class);
//        AggregateIterable<Document> aggregateBySport = getSportAggregation(dbCollection);
//        AggregateIterable<Document> sportAggregates = aggregateBySport.batchSize(500); // shouldn't be more than 500 sports
        AggregateIterable<Document> sportAggregates = AggregationQueryUtil.getAggregationBySport(collection);
        for (Document sportAggregate : sportAggregates) {
            SimulationAggregateResultElement simulationAggregateResultElement = toAggregationResultElement(new JSONObject(sportAggregate.toJson()), true);
            simulationAggregateResult.getResults().put(simulationAggregateResultElement.getSport(), simulationAggregateResultElement);
        }
        AggregateIterable<Document> allAggregations = AggregationQueryUtil.getAllAggregations(collection);
        for (Document aggregation : allAggregations) {
            SimulationAggregateResultElement simulationAggregateResultElement = toAggregationResultElement(new JSONObject(aggregation.toJson()), false);
            simulationAggregateResult.getResults().put(simulationAggregateResultElement.getSport(), simulationAggregateResultElement);
        }
        return simulationAggregateResult;
    }

    private AggregateIterable<Document> getSportAggregation(MongoCollection<Document> dbCollection) {
        String id = "{" +
                "\"sport\":\"$sport\"" +
                "}";
        return dbCollection.aggregate(Arrays.asList(
                getMatchStage(),
                getSportAggregation(id)
        ));
    }

    private AggregateIterable<Document> getAllAggregation(MongoCollection<Document> dbCollection) {
        String id = "{\"_id\": null}";
        return dbCollection.aggregate(Arrays.asList(
                getMatchStage(),
                getSportAggregation(id)
        ));
    }

    private Bson getSportAggregation(String groupidJSON) {
        return Aggregates.group(BsonDocument.parse(
                "{" +
                        "\"_id\":" + groupidJSON + "," +
                        "\"netProfit\":{" +
                        "\"$sum\":\"$profitRealized\"" +
                        "}," +
                        "\"averageProfit\":{" +
                        "\"$avg\":\"$profitRealized\"" +
                        "}," +
                        "\"totalBets\":{" +
                        "\"$sum\":\"$numBetsPlaced\"" +
                        "}," +
                        "\"averageBets\":{" +
                        "\"$avg\":\"$numBetsPlaced" +
                        "\"}," +
                        "\"eventsBetOn\":{" +
                        "\"$sum\":1" +
                        "}" +
                        "}"
        ));
    }

    private Bson getMatchStage() {
        return Aggregates.match(BsonDocument.parse(
                "{\"" +
                    "profitRealized\":{" +
                        "\"$exists\":true" +
                    "}," +
                    "\"numBetsPlaced\":{" +
                        "\"$exists\":true," +
                        "\"$gt\":0" +
                    "}" +
                "}"));
    }

    @Test
    public void testParseJsonDocument() throws Exception {
        String json =
                "{ " +
                "\"_id\" : { " +
                    "\"_id\" : { " +
                        "\"sport\" : \"E-SPORTS\" " +
                    "}, " +
                    "\"netProfit\" : -4.75, " +
                    "\"averageProfit\" : -4.75, " +
                    "\"totalBets\" : 2, " +
                    "\"averageBets\" : 2.0, " +
                    "\"eventsBetOn\" : 1 " +
                    "} " +
                "}";
        JSONObject jsonObject = new JSONObject(json);
        toAggregationResultElement(jsonObject, true);
    }

    private static SimulationAggregateResultElement toAggregationResultElement(JSONObject jsonObject, boolean isBySport) throws JSONException {
        SimulationAggregateResultElement sportSummary = new SimulationAggregateResultElement();
//        JSONObject main = jsonObject.getJSONObject("_id");
        if (isBySport) {
            sportSummary.setSport(jsonObject.getJSONObject("_id").getString("sport"));
        } else {
            sportSummary.setSport(jsonObject.getString("_id"));
        }
        sportSummary.setNetProfit(jsonObject.getDouble("netProfit"));
        sportSummary.setAverageProfit(jsonObject.getDouble("averageProfit"));
        sportSummary.setTotalBets(jsonObject.getInt("totalBets"));
        sportSummary.setAverageBets(jsonObject.getDouble("averageBets"));
        sportSummary.setEventsBetOn(jsonObject.getJSONObject("eventsBetOn").getInt("$numberLong"));
        sportSummary.setAverageFavoriteReversals(jsonObject.getDouble("averageFavoriteReversals"));
        sportSummary.setAverageWinnerOddsStandardDeviation(jsonObject.getDouble("averageWinnerOddsStandardDeviation"));
        sportSummary.setAverageLoserOddsStandardDeviation(jsonObject.getDouble("averageLoserOddsStandardDeviation"));
        sportSummary.setAverageNumOddsQuoted(jsonObject.getDouble("averageNumOddsQuoted"));

        return sportSummary;
    }

    private BettingExecutionMetaResults simulateBettingStrategy(Event event, Market market, Outcome outcome1, Outcome outcome2, List<Price> previousPrices1, List<Price> previousPrices2) {
        List<OutcomeAndPriceTick> outcomeAndPriceTicks = getMergedPrices(outcome1, outcome2, previousPrices1, previousPrices2);
        String winningOutcomeId = market.getBettingSession() != null && market.getBettingSession().getWinningOutcomeId() != null ?
                market.getBettingSession().getWinningOutcomeId() : getWinningOutcome(market, outcomeAndPriceTicks);
        if (winningOutcomeId == null) {
            return null;
        }
        market.setBettingSession(null);
        event.setBettingEnabled(true);
        BettingExecutionMetaResults bettingExecutionMetaResults = executeBettingStrategy(event, market, outcome1, outcome2, outcomeAndPriceTicks, winningOutcomeId);
        if (market.getBettingSession() == null) {
            // bet was never made
            return null;
        }
        return bettingExecutionMetaResults;
    }

    private List<OutcomeAndPriceTick> getMergedPrices(Outcome outcome1, Outcome outcome2, List<Price> previousPrices1, List<Price> previousPrices2) {
        List<OutcomeAndPriceTick> outcomeAndPriceTicks = new ArrayList<>();
        for (Price price : previousPrices1) {
            outcomeAndPriceTicks.add(new OutcomeAndPriceTick(outcome1, price));
        }
        for (Price price : previousPrices2) {
            outcomeAndPriceTicks.add(new OutcomeAndPriceTick(outcome2, price));
        }
        // TODO: make sure sorted in correct direction
        outcomeAndPriceTicks.sort(Comparator.comparing(a -> a.price.getCreated()));
        return outcomeAndPriceTicks;
    }

    private BettingExecutionMetaResults executeBettingStrategy(Event event, Market market, Outcome outcome1, Outcome outcome2, List<OutcomeAndPriceTick> outcomeAndPriceTicks, String winningOutcomeId) {
        BettingExecutionMetaResults bettingExecutionMetaResults = new BettingExecutionMetaResults(event, market, outcome1, outcome2, winningOutcomeId);
        boolean initialUnderdogSet = false;
        String lastFavoriteOutcomeId = outcomeAndPriceTicks.get(0).outcome.getId();
        for (int i = 0; i < outcomeAndPriceTicks.size(); i++) {
            OutcomeAndPriceTick currentTick = outcomeAndPriceTicks.get(i);
            initialUnderdogSet = analyzeTick(winningOutcomeId, bettingExecutionMetaResults, initialUnderdogSet, lastFavoriteOutcomeId, currentTick);
            Outcome opposingOutcome = currentTick.outcome.getId().equals(outcome1.getId()) ? outcome2 : outcome1;
            bettingFacilitatorService.updateBettingSession(event, market, currentTick.outcome, opposingOutcome, currentTick.price, Strategy.BASIC);
            if (currentTick.price.getAmerican() < 100) {
                lastFavoriteOutcomeId = currentTick.outcome.getId();
            }
        }
        if (market.getBettingSession() != null) {
            market.getBettingSession().setWinningOutcomeId(winningOutcomeId);
            performPostAnalysis(market, bettingExecutionMetaResults, outcomeAndPriceTicks);
        }
        return bettingExecutionMetaResults;
    }

    private void performPostAnalysis(Market market, BettingExecutionMetaResults bettingExecutionMetaResults, List<OutcomeAndPriceTick> outcomeAndPriceTicks) {
        String winningOutcomeId = market.getBettingSession().getWinningOutcomeId();
        String losingOutcomeId = market.getBettingSession().getOutcome1Id().equals(winningOutcomeId) ?
                market.getBettingSession().getOutcome2Id() : market.getBettingSession().getOutcome1Id();
        double[] fractionalWinnerOdds = toFractionalOdds(outcomeAndPriceTicks, winningOutcomeId);
        bettingExecutionMetaResults.winnerOddsStandardDeviation = new StandardDeviation().evaluate(fractionalWinnerOdds);
        double[] fractionalLoserOdds = toFractionalOdds(outcomeAndPriceTicks, losingOutcomeId);
        bettingExecutionMetaResults.loserOddsStandardDeviation = new StandardDeviation().evaluate(fractionalLoserOdds);
        bettingExecutionMetaResults.numBetsPlaced = market.getBettingSession().getPositions().values().stream()
                .reduce(0, (subtotal, position) -> subtotal + position.getBets().size(), Integer::sum);
        bettingExecutionMetaResults.numOddsQuoted = outcomeAndPriceTicks.size();
        bettingExecutionMetaResults.profitRealized = market.getFinalNetProfit();
    }

    private double[] toFractionalOdds(List<OutcomeAndPriceTick> outcomeAndPriceTicks, String outcomeId) {
        return outcomeAndPriceTicks.stream()
                .filter(outcomeAndPriceTick -> outcomeAndPriceTick.outcome.getId().equals(outcomeId))
                .map(outcomeAndPriceTick -> Market.toFractionalOdds(outcomeAndPriceTick.price.getAmerican()))
                .mapToDouble(x -> x)
                .toArray();
    }

    private boolean analyzeTick(String winningOutcomeId, BettingExecutionMetaResults bettingExecutionMetaResults, boolean initialUnderdogSet, String lastFavoriteOutcomeId, OutcomeAndPriceTick currentTick) {
        if (currentTick.price.getAmerican() < 100
                && !currentTick.outcome.getId().equals(lastFavoriteOutcomeId)) {
            bettingExecutionMetaResults.favoriteReversals++;
        }
        if (currentTick.outcome.getId().equals(winningOutcomeId)) {
            if (!initialUnderdogSet) {
                bettingExecutionMetaResults.winnerWasInitialUnderdog = currentTick.price.getAmerican() > 0;
                initialUnderdogSet = true;
            }
            if (currentTick.price.getAmerican() > bettingExecutionMetaResults.worstAmericanOddsForWinner) {
                bettingExecutionMetaResults.worstAmericanOddsForWinner = currentTick.price.getAmerican();
            }
        } else {
            if (currentTick.price.getAmerican() < bettingExecutionMetaResults.bestOddsForLoser) {
                bettingExecutionMetaResults.bestOddsForLoser = currentTick.price.getAmerican();
            }
        }
        return initialUnderdogSet;
    }

    // EVENT COLUMNS
    // - base obj columns: sport, eventId, winning outcome description
    // - num odds quotes
    // - profit
    // - num bets placed
    // - worst odds quote (for winner)
    // - best odds quote (for loser)
    // - num favorite reversals and:
    // - standard deviation for winner odds --> need to convert to fractional first
    // - standard deviation for loser odds --> need to convert to fractional first
    // - column for multiple betting strategies?? -> probably not
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class BettingExecutionMetaResults {
////        @Id
////        private ObjectId id = new ObjectId();
//        private Long eventId;
//        private String eventDescription;
//        private String winningOutcomeDescription;
//        private String sport;
//        private double profitRealized;
//
//        boolean winnerWasInitialUnderdog;
//        private int favoriteReversals;
//        private int worstAmericanOddsForWinner = -1000000;
//        private int bestOddsForLoser = 1000000;
//        private double winnerOddsStandardDeviation;
//        private double loserOddsStandardDeviation;
//        private int numBetsPlaced;
//        private int numOddsQuoted;
//
//        public BettingExecutionMetaResults(Event event, Market market, Outcome outcome1, Outcome outcome2, String winningOutcomeId) {
//            this.eventId = event.getId();
//            this.eventDescription = event.getDescription();
//            Outcome winningOutcome = outcome1.getId().equals(winningOutcomeId) ? outcome1 : outcome2;
//            this.winningOutcomeDescription = winningOutcome.getDescription();
//            this.sport = event.getSport();
//        }
//    }

    private String getWinningOutcome(Market market, List<OutcomeAndPriceTick> outcomeAndPriceTicks) {
        for (int i = outcomeAndPriceTicks.size()-1; i >= 0; i--) {
            OutcomeAndPriceTick lastOutcomeAndPriceTick = outcomeAndPriceTicks.get(i);
            if (lastOutcomeAndPriceTick.price.getAmerican() < 0) {
                return lastOutcomeAndPriceTick.outcome.getId();
            }
        }
        return null;
    }

    @AllArgsConstructor
    private class OutcomeAndPriceTick {
        Outcome outcome;
        Price price;
    }

}
