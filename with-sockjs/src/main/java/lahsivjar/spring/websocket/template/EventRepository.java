package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<Event, String> {
}

