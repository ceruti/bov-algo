package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import lahsivjar.spring.websocket.template.util.LiveFeedUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
// TODO: combine this with liveFeedUpdateService... no reason to split these out
public class LiveOddsUpdateService {

    private EventBook eventBook;
    private LiveFeedUpdateService liveFeedUpdateService;

    @Autowired
    public LiveOddsUpdateService(EventBook eventBook, LiveFeedUpdateService liveFeedUpdateService) {
        this.eventBook = eventBook;
        this.liveFeedUpdateService = liveFeedUpdateService;
    }

    public void updateEventBook(String wireMessage) {
        Collection<Long> eventIds = LiveFeedUpdateService.getEventIds(wireMessage);
        for (Long eventId : eventIds) {
            if (eventId != null && this.eventBook.getBook().containsKey(eventId)) {
                Event existingEvent = this.eventBook.getBook().get(eventId);
                if (liveFeedUpdateService.updateEvent(existingEvent, wireMessage)) {
                    existingEvent.markUpdated();
                }
            }
        }
    }


}
