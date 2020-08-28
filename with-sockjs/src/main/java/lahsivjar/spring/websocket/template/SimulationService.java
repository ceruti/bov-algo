package lahsivjar.spring.websocket.template;

import com.mongodb.BasicDBObject;
import lahsivjar.spring.websocket.template.model.BettingExecutionMetaResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
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
    public List<BettingExecutionMetaResults> getSimulation(String id, String sortBy, Boolean sortDescending) {
        Query query = new Query();
        query.limit(100); // TODO: use record limit
        if (sortBy != null && !sortBy.isEmpty()) {
            boolean _sortDescending = sortDescending == null ? true : sortDescending;
            Sort.Direction sortDirection = _sortDescending ? Sort.Direction.DESC : Sort.Direction.ASC;
            query.with(new Sort(sortDirection, sortBy));
        }
        List<BettingExecutionMetaResults> bettingExecutionMetaResults = mongoTemplate.find(query, BettingExecutionMetaResults.class, id);
        return bettingExecutionMetaResults;
    }
}
