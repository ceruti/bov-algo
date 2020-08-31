package com.ceruti.bov;

import com.ceruti.bov.model.Event;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class EventSyncService {

    private EventRepository eventRepository;
    private EventBook eventBook;
    private MongoTemplate mongoTemplate;

    @Autowired
    public EventSyncService(EventRepository eventRepository, EventBook eventBook, MongoTemplate mongoTemplate) {
        this.eventRepository = eventRepository;
        this.eventBook = eventBook;
        this.mongoTemplate = mongoTemplate;

        Date thirtyMinutesAgo = new DateTime().minusMinutes(30).toDate();
        List<Event> events = this.mongoTemplate.find(Query.query(Criteria.where("lastUpdated").gte(thirtyMinutesAgo)), Event.class, "event");

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

    // every 5 minutes, take stale events out of memory (they will certainly be in the DB by now)
    // we don't want the heap size to grow out of control, and we want to keep the UI neat
    @Scheduled(fixedDelay = 1000*60*5)
    public void refresh() {
        eventBook.getBook().keySet().removeIf(eventId -> {
            DateTime lastUpdated = new DateTime(eventBook.getBook().get(eventId).getLastUpdated());
            DateTime thirtyMinutesAgo = new DateTime().minusMinutes(30);
            return lastUpdated.isBefore(thirtyMinutesAgo);
        });
    }


}
