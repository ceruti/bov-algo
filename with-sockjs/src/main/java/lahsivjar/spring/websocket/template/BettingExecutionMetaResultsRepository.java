package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.BettingExecutionMetaResults;
import lahsivjar.spring.websocket.template.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BettingExecutionMetaResultsRepository extends MongoRepository<BettingExecutionMetaResults, String> {
}
