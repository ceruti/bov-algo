package com.ceruti.bov;

import com.ceruti.bov.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
// TODO: combine this with liveFeedUpdateService... no reason to split these out
public class LiveOddsUpdateService {

    private EventBook eventBook;
    private LiveFeedUpdateService liveFeedUpdateService;
    private SimpMessagingTemplate simpMessagingTemplate;
    private ExecutorService executorService;

    @Autowired
    public LiveOddsUpdateService(EventBook eventBook,
                                 LiveFeedUpdateService liveFeedUpdateService,
                                 SimpMessagingTemplate simpMessagingTemplate) {
        this.eventBook = eventBook;
        this.liveFeedUpdateService = liveFeedUpdateService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.executorService = Executors.newFixedThreadPool(25);
    }

    public void updateEventBookAsync(String wireMessage) {
        executorService.submit(() -> {
            updateEventBook(wireMessage);
        });
    }

    public void updateEventBook(String wireMessage) {
        if (wireMessage.equalsIgnoreCase("{\"id\":0}")) {
            return;
        }
        try {
            Collection<Long> eventIds = LiveFeedUpdateService.getEventIds(wireMessage);
            if (eventIds == null) return;
            for (Long eventId : eventIds) {
                if (eventId != null && this.eventBook.getBook().containsKey(eventId)) {
                    Event existingEvent = this.eventBook.getBook().get(eventId);
                    existingEvent.getRawWireMessages().add(wireMessage);
                    if (liveFeedUpdateService.updateEvent(existingEvent, wireMessage)) {
                        existingEvent.markUpdated();
                        simpMessagingTemplate.convertAndSend("/topic/all", existingEvent);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Unable to update event book!");
            e.printStackTrace();
            System.err.println("Full wire message: "+wireMessage);
        }
    }


}
