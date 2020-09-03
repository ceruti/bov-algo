package com.ceruti.bov;

import com.ceruti.bov.model.Event;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
@Profile("!test")
public class EventSyncService {

    private EventRepository eventRepository;
    private EventBook eventBook;

    @Autowired
    public EventSyncService(EventRepository eventRepository, EventBook eventBook) {
        this.eventRepository = eventRepository;
        this.eventBook = eventBook;
    }

    @Scheduled(fixedDelay = 10000)
    public synchronized void sync() {
        if (!eventBook.isEnableUpdates()) {
            return;
        }
        System.out.println("Syncing to database...");
        Collection<Event> values = eventBook.getBook().values();
        this.eventRepository.save(values);
    }

    // every 5 minutes, take stale events out of memory (they will certainly be in the DB by now)
    // we don't want the heap size to grow out of control, and we want to keep the UI neat
    @Scheduled(fixedDelay = 1000*60*5)
    public void refresh() {
        eventBook.getBook().keySet().removeIf(eventId -> {
            Event event = eventBook.getBook().get(eventId);
            if (event.getMarkets().values().stream().anyMatch(market -> market.getBettingSession() != null)) {
                return false;
            }
            DateTime lastUpdated = new DateTime(event.getLastUpdated());
            DateTime thirtyMinutesAgo = new DateTime().minusMinutes(30);
            return lastUpdated.isBefore(thirtyMinutesAgo);
        });
    }


}
