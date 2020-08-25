package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import lahsivjar.spring.websocket.template.util.LiveOddsUpdateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static lahsivjar.spring.websocket.template.util.LiveOddsUpdateUtil.updateEvent;

@Component
public class LiveOddsUpdateService {

    private EventBook eventBook;

    @Autowired
    public LiveOddsUpdateService(EventBook eventBook) {
        this.eventBook = eventBook;
    }

    public void updateEventBook(String wireMessage) {
        Long eventId = LiveOddsUpdateUtil.getEventId(wireMessage);
        if (eventId != null && this.eventBook.getBook().containsKey(eventId)) {
            Event existingEvent = this.eventBook.getBook().get(eventId);
            updateEvent(existingEvent, wireMessage);
        }

    }


}
