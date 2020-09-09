package com.ceruti.bov;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.model.Outcome;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
public class EventBook {

    private static final Set<String> initiateAutoBettingBlacklist = new HashSet<>(Arrays.asList(
            "TENNIS"
    )); // we won't automatically initiate auto-betting on these sports... but once a manual bet is placed, auto betting is allowed
    // We do this because we don't have an automated solution for distinguishing between men and women's tennis.

    private NullAwareBeanUtilsBean nullAwareBeanUtilsBean;
    private SimpMessagingTemplate simpMessagingTemplate;
    private MongoTemplate mongoTemplate;

    private boolean enableUpdates = true;

    @Autowired
    public EventBook(NullAwareBeanUtilsBean nullAwareBeanUtilsBean, SimpMessagingTemplate simpMessagingTemplate, MongoTemplate mongoTemplate) {
        this.nullAwareBeanUtilsBean = nullAwareBeanUtilsBean;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.mongoTemplate = mongoTemplate;
        book = new HashMap<>();
        Date thirtyMinutesAgo = new DateTime().minusMinutes(30).toDate();
        List<Event> events = this.mongoTemplate.find(Query.query(Criteria.where("lastUpdated").gte(thirtyMinutesAgo)), Event.class, "event");
        for (Event event : events) {
            if (System.getProperty("spring.profiles.active") != null && System.getProperty("spring.profiles.active").contains("live")) {
                event.setBettingEnabled(false);
            }
            book.put(event.getId(), event);
        }
        System.out.println("Event book initialized from database.");
    }

    private Map<Long, Event> book;

    public Map<Long, Event> getBook() {
        return book;
    }

    public boolean isOnInitiateBettingSessionBlacklist(Event event) {
        return initiateAutoBettingBlacklist.contains(EventBook.getEquivalentKey(event.getSport()));
    }

    public Map<String, Map<Long, Event>> getLiveEvents() {
        Map<String, Map<Long, Event>> liveEvents = new HashMap<>();
        this.book.values().parallelStream().forEach(event -> {
            Date lastUpdated = event.getLastUpdated();
            if (lastUpdated != null) {
                DateTime lastUpdatedJoda = new DateTime(lastUpdated);
                DateTime now = new DateTime();
                if (lastUpdatedJoda.isAfter(now.minusMinutes(30))) {
                    String sport = getEquivalentKey(event.getSport());
                    Map<Long, Event> mapForSport = liveEvents.get(sport);
                    if (mapForSport == null) {
                        mapForSport = new HashMap<>();
                        liveEvents.put(sport, mapForSport);
                    }
                    mapForSport.put(event.getId(), event);
                }
            }
        });
        return liveEvents;
    }

    public static String getEquivalentKey(String sport) {
        switch(sport) {
            case "TENN":
                return "TENNIS";
            case "HCKY":
                return "HOCKEY";
            case "SOCC":
                return "SOCCER";
            case "BASK":
                return "BASKETBALL";
            case "TABL":
                return "TABLETENNIS";
            case "BASE":
                return "BASEBALL";
            case "ESPT":
                return "E-SPORTS";
            case "DART":
                return "DARTS";
            case "RUGU":
                return "RUGBY";
            case "CRIC":
                return "CRICKET";
            case "FOOT":
                return "FOOTBALL";
            case "VOLL":
                return "VOLLEYBALL";
            default:
                return sport;
        }
    }

    public void setBook(Map<Long, Event> book) {
        this.book = book;
    }

    public void addEvent(Event event) {
        System.out.println("New event found: " + event.getDescription());
        book.put(event.getId(), event);
        event.setSport(getEquivalentKey(event.getSport()));
        simpMessagingTemplate.convertAndSend("/topic/all", event);
    }

    public void updateEvent(Event event) throws InvocationTargetException, IllegalAccessException {
        Event existingEvent = book.get(event.getId());
        nullAwareBeanUtilsBean.updateEvent(existingEvent, event);
    }

    public boolean isEnableUpdates() {
        return enableUpdates;
    }

    public void setEnableUpdates(boolean enableUpdates) {
        this.enableUpdates = enableUpdates;
    }

    public void enableEventForBetting(Long eventId) {
        this.book.get(eventId).enableBetting();
    }

    public void disableEventForBetting(Long eventId) {
        this.book.get(eventId).disableBetting();
    }

    public void enableOutcomeForBetting(Long eventId, String marketId, String outcomeId, boolean forceBetting) {
        Outcome outcome = this.book.get(eventId).getMarkets().get(marketId).getOutcomes().get(outcomeId);
        outcome.enableBetting();
        outcome.setForceBettingEnabled(forceBetting);
    }

    public void disableOutcomeForBetting(Long eventId, String marketId, String outcomeId) {
        Outcome outcome = this.book.get(eventId).getMarkets().get(marketId).getOutcomes().get(outcomeId);
        outcome.disableBetting();
        outcome.setForceBettingEnabled(false);
    }
}
