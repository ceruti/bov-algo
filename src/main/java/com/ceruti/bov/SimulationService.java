package com.ceruti.bov;

import com.ceruti.bov.model.BettingExecutionMetaResults;
import com.ceruti.bov.model.SimulationAggregateResult;
import com.ceruti.bov.model.SimulationPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SimulationService {

    private MongoTemplate mongoTemplate;

    @Autowired
    public SimulationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<String> getSimulationids() {
        Set<String> collectionNames = this.mongoTemplate.getDb().getCollectionNames();
        return collectionNames.stream().filter(collectionName -> collectionName.startsWith("bettingExecutionMetaResults")).collect(Collectors.toList());
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
}
