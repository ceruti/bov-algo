package com.ceruti.bov;

import com.ceruti.bov.model.BettingExecutionMetaResults;
import com.ceruti.bov.model.SimulatedEvent;
import com.ceruti.bov.model.SimulationAggregateResult;
import com.ceruti.bov.model.SimulationPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SimulationService {

    private final SimulatedEventRepository simulatedEventRepository;
    private MongoTemplate mongoTemplate;

    @Autowired
    public SimulationService(MongoTemplate mongoTemplate, SimulatedEventRepository simulatedEventRepository) {
        this.mongoTemplate = mongoTemplate;
        this.simulatedEventRepository = simulatedEventRepository;
    }

    public List<SimulationAggregateResult> getSimulations() { // sort by best first
        return this.mongoTemplate.findAll(SimulationAggregateResult.class, "simulationAggregations")
                .stream()
                .filter(result -> result.getResults().get("ALL").getEventsBetOn() > 100)
                .sorted((aggA, aggB) -> Double.compare(getMedianProfit(aggB), getMedianProfit(aggA)))
                .collect(Collectors.toList());
    }

    private double getMedianProfit(SimulationAggregateResult simulationAggregateResult) {
        return simulationAggregateResult.getResults().get("ALL").getMedianProfit();
    }

    // TODO: add record limit
    public SimulationPage getSimulation(String id, String sortBy, Boolean sortDescending, int page, int pageSize, String sportKey) {
        Query query = new Query();
        if (sportKey != null && !sportKey.isEmpty()) {
            query.addCriteria(Criteria.where("sport").is(sportKey));
        }
        query.limit(pageSize);
        query.skip(page*pageSize);
        if (sortBy != null && !sortBy.isEmpty()) {
            boolean _sortDescending = sortDescending == null ? true : sortDescending;
            Sort.Direction sortDirection = _sortDescending ? Sort.Direction.DESC : Sort.Direction.ASC;
            query.with(new Sort(sortDirection, sortBy));
        }
        List<BettingExecutionMetaResults> bettingExecutionMetaResults = mongoTemplate.find(query, BettingExecutionMetaResults.class, id);
        Query queryWithoutLimitAndSkip = new Query();
        if (sportKey != null && !sportKey.isEmpty()) {
            queryWithoutLimitAndSkip.addCriteria(Criteria.where("sport").is(sportKey));
        }
        long totalRecords = mongoTemplate.count(queryWithoutLimitAndSkip, id);
        long pages = (long) Math.ceil((1.0 * totalRecords) / pageSize);
        return new SimulationPage(getSimulationAggregate(id), bettingExecutionMetaResults, pages);
    }

    public SimulationAggregateResult getSimulationAggregate(String simulationId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(simulationId));
        return this.mongoTemplate.findOne(query, SimulationAggregateResult.class, "simulationAggregations");
    }

    public SimulatedEvent getSimulatedEvent(Long simulatedEventId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(simulatedEventId));
        return this.mongoTemplate.findOne(query, SimulatedEvent.class, "simulatedEvent");
    }
}
