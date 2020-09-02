package integration;

import com.ceruti.bov.*;
import com.ceruti.bov.model.*;
import com.ceruti.bov.util.EventParseUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.bson.Document;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import static com.mongodb.client.model.Filters.exists;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StrategyAnalysisIntegrationTests {

    public static final String SIMULATION_PREFIX = "BASIC-WITH-TIME-CONTROLS-ALLOW_TENNIS";

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

    @Autowired
    LiveFeedUpdateService liveFeedUpdateService;

    @Autowired
    MongoOperations mongoOperations;

    @Test
//    @Ignore
    public void renameCollection() {
        eventBook.setEnableUpdates(false);
        mongoTemplate.getDb().getCollection("bettingExecutionMetaResults-1598637977138").rename("meta");
    }

//    @Test
    public void dropBadCollections() {
        for (String collectionName : mongoTemplate.getCollectionNames()) {
            if (collectionName.startsWith("bettingExecutionMetaResults-")) {
                mongoTemplate.dropCollection(collectionName);
            }
        }
    }

    @Test
//    @Ignore
    public void testStrategy() throws Exception {
        eventBook.setEnableUpdates(false);
        Query query = new Query();
        query.addCriteria(Criteria.where("version").is("v2.0"));
        List<Event> events = mongoTemplate.find(query, Event.class, "event");
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
        if (bettingExecutionMetaResultsBuffer.size() > 0) {
            analysisRepository.save(bettingExecutionMetaResultsBuffer);
        }
        String collectionName = "bettingExecutionMetaResults-" + SIMULATION_PREFIX + new DateTime().getMillis();
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

    @Test
    public void wireMessageReplayTest() throws JsonProcessingException, JSONException {
        Long eventId = 7651191L; // TODO: change this as needed for debugging
        Query findEvent = new Query();
        findEvent.addCriteria(Criteria.where("_id").is(eventId));
        Event actualDbEvent = this.mongoTemplate
                .findOne(findEvent, Event.class, "event");
        Event replaySimulationEvent = getWireMessageReplaySimulatedEvent(eventId, actualDbEvent);

        // TODO: delete this!!
        replaySimulationEvent.setId(9999L);
        this.mongoTemplate.save(replaySimulationEvent, "event");

        System.out.print("");
    }

    private Event getWireMessageReplaySimulatedEvent(Long eventId, Event actualDbEvent) {
        Event replaySimulationEvent = new Event();
        replaySimulationEvent.setId(actualDbEvent.getId());
        replaySimulationEvent.setCompetitionId(actualDbEvent.getCompetitionId());
        replaySimulationEvent.setDescription(actualDbEvent.getDescription());
        replaySimulationEvent.setCompetitors(actualDbEvent.getCompetitors());
        replaySimulationEvent.setSport(actualDbEvent.getSport());
        replaySimulationEvent.setLive(true);
        replaySimulationEvent.setRawWireMessages(actualDbEvent.getRawWireMessages());
//        replaySimulationEvent.setRawEventSummaries(actualDbEvent.getRawEventSummaries());
        Map<String, Market> markets = new HashMap<>();
        for (String marketId : actualDbEvent.getMarkets().keySet()) {
            // need to be careful to clear in-session values
            Market actualDBMarket = actualDbEvent.getMarkets().get(marketId);
            actualDBMarket.setBettingSession(null);
            for (String outcomeId : actualDBMarket.getOutcomes().keySet()) {
                Outcome outcome = actualDBMarket.getOutcomes().get(outcomeId);
                outcome.setPrice(null);
                outcome.setPreviousPrices(null);
            }
            markets.put(actualDBMarket.getId(), actualDBMarket);
        }
        replaySimulationEvent.setMarkets(markets);
        this.eventBook.getBook().put(eventId, replaySimulationEvent);
        for (String rawWireMessage : actualDbEvent.getRawWireMessages()) {
            liveFeedUpdateService.updateEvent(replaySimulationEvent, rawWireMessage);
        }
        return replaySimulationEvent;
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
        List<OutcomeAndPriceTick> outcomeAndPriceTicks = getMergedPrices(outcome1, outcome2, previousPrices1, previousPrices2, event.getRawWireMessages());
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

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private List<OutcomeAndPriceTick> getMergedPrices(Outcome outcome1, Outcome outcome2, List<Price> previousPrices1, List<Price> previousPrices2, List<String> rawWireMessages) {
        // IMPORTANT: need to match wire message order for replay
        List<LiveFeedUpdateService.WireMessageType2> wireMessages = rawWireMessages.stream()
                .filter(rawMessage -> rawMessage.contains("}|{"))
                .map(LiveFeedUpdateService::getWireMessage)
                .filter(wireMessage -> wireMessage instanceof LiveFeedUpdateService.WireMessageType2
                        && ((LiveFeedUpdateService.WireMessageType2) wireMessage).getNewPriceId() != null
                )
                .map(wireMessage -> (LiveFeedUpdateService.WireMessageType2) wireMessage)
                .filter(wireMessage -> wireMessage.getOutcomeId().equals(outcome1.getId()) || wireMessage.getOutcomeId().equals(outcome2.getId()))
                .collect(Collectors.toList());

        Map<String, Integer> priceIdToOriginalWireMesageIndex = new HashMap<>();
        for (int i=0; i < wireMessages.size(); i++) {
            priceIdToOriginalWireMesageIndex.put(wireMessages.get(i).getNewPriceId(), i);
        }
        OutcomeAndPriceTick[] outcomeAndPriceTicks = new OutcomeAndPriceTick[rawWireMessages.size()];
        for (Price price : previousPrices1) {
            int index = priceIdToOriginalWireMesageIndex.get(price.getId());
            outcomeAndPriceTicks[index] = new OutcomeAndPriceTick(outcome1, price);
        }
        for (Price price : previousPrices2) {
            int index = priceIdToOriginalWireMesageIndex.get(price.getId());
            outcomeAndPriceTicks[index] = new OutcomeAndPriceTick(outcome2, price);
        }

        int finalPriceOutcome1Index = priceIdToOriginalWireMesageIndex.get(outcome1.getPrice().getId());
        outcomeAndPriceTicks[finalPriceOutcome1Index] = new OutcomeAndPriceTick(outcome1, outcome1.getPrice());

        int finalPriceOutcome2Index = priceIdToOriginalWireMesageIndex.get(outcome2.getPrice().getId());
        outcomeAndPriceTicks[finalPriceOutcome2Index] = new OutcomeAndPriceTick(outcome2, outcome1.getPrice());

        List<OutcomeAndPriceTick> result = Arrays.stream(outcomeAndPriceTicks)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return result;
    }

    private BettingExecutionMetaResults executeBettingStrategy(Event event, Market market, Outcome outcome1, Outcome outcome2, List<OutcomeAndPriceTick> outcomeAndPriceTicks, String winningOutcomeId) {
        BettingExecutionMetaResults bettingExecutionMetaResults = new BettingExecutionMetaResults(event, market, outcome1, outcome2, winningOutcomeId);
        boolean initialUnderdogSet = false;
        String lastFavoriteOutcomeId = outcomeAndPriceTicks.get(0).outcome.getId();
        outcome1.setPrice(null);
        outcome2.setPrice(null);
        for (int i = 0; i < outcomeAndPriceTicks.size(); i++) {
            OutcomeAndPriceTick currentTick = outcomeAndPriceTicks.get(i);
            currentTick.outcome.setPrice(currentTick.price);
            if (currentTick.outcome.getId().equals(outcome1)) {
                outcome1.setPrice(currentTick.price);
            }
            if (currentTick.outcome.getId().equals(outcome2)) {
                outcome2.setPrice(currentTick.price);
            }
            updateClockAndScore(event, currentTick);
            initialUnderdogSet = analyzeTick(winningOutcomeId, bettingExecutionMetaResults, initialUnderdogSet, lastFavoriteOutcomeId, currentTick);
            Outcome opposingOutcome = currentTick.outcome.getId().equals(outcome1.getId()) ? outcome2 : outcome1;
            market.getOutcomes().put(currentTick.outcome.getId(), currentTick.outcome);
            bettingFacilitatorService.updateBettingSession(event, market, currentTick.outcome, opposingOutcome, currentTick.price, LiveFeedUpdateService.BETTING_STRATEGY);
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

    private void updateClockAndScore(Event event, OutcomeAndPriceTick currentTick) {
        if (currentTick.price.getClockAtTimeOfPrice() != null) {
            event.setClock(currentTick.price.getClockAtTimeOfPrice());
        }
        if (currentTick.price.getHomeScoreAtTimeOfPrice() != null) {
            event.setHomeScore(currentTick.price.getHomeScoreAtTimeOfPrice());
        }
        if (currentTick.price.getVisitorScoreAtTimeOfPrice() != null) {
            event.setVisitorScore((currentTick.price.getVisitorScoreAtTimeOfPrice()));
        }
        event.setCurrentPeriodHomeScore(currentTick.price.getCurrentPeriodHomeScoreAtTimeOfPrice());
        event.setCurrentPeriodVisitorScore(currentTick.price.getCurrentPeriodVisitorScoreAtTimeOfPrice());
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

    private String getWinningOutcome(Market market, List<OutcomeAndPriceTick> outcomeAndPriceTicks) {
        for (int i = outcomeAndPriceTicks.size()-1; i >= 0; i--) {
            OutcomeAndPriceTick lastOutcomeAndPriceTick = outcomeAndPriceTicks.get(i);
            if (lastOutcomeAndPriceTick.price.getAmerican() < 0) {
                return lastOutcomeAndPriceTick.outcome.getId();
            }
        }
        return null;
    }

    private class OutcomeAndPriceTick {
        public OutcomeAndPriceTick(Outcome outcome, Price price) {
            this.outcome = outcome;
            this.price = price;
        }

        Outcome outcome;
        Price price;
    }

}
