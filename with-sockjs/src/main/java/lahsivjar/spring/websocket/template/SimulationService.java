package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.BettingExecutionMetaResults;
import lahsivjar.spring.websocket.template.model.SimulationAggregateResult;
import lahsivjar.spring.websocket.template.model.SimulationPage;
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
        query.limit(pageSize); // TODO: use record limit
        query.skip(page*pageSize);
        if (sortBy != null && !sortBy.isEmpty()) {
            boolean _sortDescending = sortDescending == null ? true : sortDescending;
            Sort.Direction sortDirection = _sortDescending ? Sort.Direction.DESC : Sort.Direction.ASC;
            query.with(new Sort(sortDirection, sortBy));
        }
        if (sportKey != null && !sportKey.isEmpty()) {
            query.addCriteria(Criteria.where("sport").is(sportKey));
        }
        List<BettingExecutionMetaResults> bettingExecutionMetaResults = mongoTemplate.find(query, BettingExecutionMetaResults.class, id);
        long totalRecords = mongoTemplate.count(new Query(), id);
        long pages = totalRecords / pageSize;
        return new SimulationPage(getSimulationAggregate(id), bettingExecutionMetaResults, pages);
    }

    public SimulationAggregateResult getSimulationAggregate(String simulationId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(simulationId));
        return this.mongoTemplate.findOne(query, SimulationAggregateResult.class, "simulationAggregations");
    }
}
