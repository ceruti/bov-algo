package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class EventSyncService {

    private EventRepository eventRepository;
    private EventBook eventBook;

    @Autowired
    public EventSyncService(EventRepository eventRepository, EventBook eventBook) {
        this.eventRepository = eventRepository;
        this.eventBook = eventBook;
    }

    @Scheduled(fixedDelay = 10000)
    public void sync() {
        Collection<Event> values = eventBook.getBook().values();
        this.eventRepository.save(values);
    }

}
