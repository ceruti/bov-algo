package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class EventSyncService {

    private EventRepository eventRepository;
    private EventBook eventBook;

    @Autowired
    public EventSyncService(EventRepository eventRepository, EventBook eventBook) {
        this.eventRepository = eventRepository;
        this.eventBook = eventBook;

        List<Event> events = this.eventRepository.findAll();
        for (Event event : events) {
            this.eventBook.getBook().put(event.getId(), event);
        }
        System.out.println("Event book initialized from database.");
    }

    @Scheduled(fixedDelay = 10000)
    public void sync() {
        if (!eventBook.isEnableUpdates()) {
            return;
        }
        System.out.println("Syncing to database...");
        Collection<Event> values = eventBook.getBook().values();
        this.eventRepository.save(values);
    }

}
