package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import lahsivjar.spring.websocket.template.util.LiveOddsUpdateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class LiveOddsUpdateService {

    private EventBook eventBook;

    @Autowired
    public LiveOddsUpdateService(EventBook eventBook) {
        this.eventBook = eventBook;
    }

    public void updateEventBook(String wireMessage) {
        Collection<Long> eventIds = LiveOddsUpdateUtil.getEventIds(wireMessage);
        for (Long eventId : eventIds) {
            if (eventId != null && this.eventBook.getBook().containsKey(eventId)) {
                Event existingEvent = this.eventBook.getBook().get(eventId);
                LiveOddsUpdateUtil.updateEvent(existingEvent, wireMessage);
            }
        }
    }


}
