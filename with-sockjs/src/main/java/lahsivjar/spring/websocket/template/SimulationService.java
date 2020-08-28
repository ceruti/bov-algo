package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.BettingExecutionMetaResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    public List<BettingExecutionMetaResults> getSimulation(String simulationId) {
        return this.mongoTemplate.findAll(BettingExecutionMetaResults.class, simulationId);
    }
}
