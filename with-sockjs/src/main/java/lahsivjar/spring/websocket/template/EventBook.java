package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventBook {

    private NullAwareBeanUtilsBean nullAwareBeanUtilsBean;

    private boolean enableUpdates = true;

    @Autowired
    public EventBook(NullAwareBeanUtilsBean nullAwareBeanUtilsBean) {
        this.nullAwareBeanUtilsBean = nullAwareBeanUtilsBean;
        book = new HashMap<>();
    }

    private Map<Long, Event> book;

    public Map<Long, Event> getBook() {
        return book;
    }

    public Map<String, Map<Long, Event>> getLiveEvents() {
        Map<String, Map<Long, Event>> liveEvents = new HashMap<>();
        this.book.values().parallelStream().forEach(event -> {
            Date lastUpdated = event.getLastUpdated();
            if (lastUpdated != null && (event.getGameStatus() == null || !event.getGameStatus().equalsIgnoreCase("GAME_END"))) {
                DateTime lastUpdatedJoda = new DateTime(lastUpdated);
                DateTime now = new DateTime();
                if (lastUpdatedJoda.isAfter(now.minusMinutes(30))) {
                    String sport = getEquivalentKey(event.getSport());
                    if (!liveEvents.containsKey(sport)) {
                        liveEvents.put(sport, new HashMap<>());
                    }
                    Map<Long, Event> mapForSport = liveEvents.get(sport);
                    mapForSport.put(event.getId(), event);
                }
            }
        });
        return liveEvents;
    }

    private String getEquivalentKey(String sport) {
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

    public void enableEventForBetting(String eventId) {
        this.book.get(eventId).enableBetting();
    }

    public void disableEventForBetting(String eventId) {
        this.book.get(eventId).disableBetting();
    }

    public void enableOutcomeForBetting(String eventId, String marketId, String outcomeId) {
        this.book.get(eventId).getMarkets().get(marketId).getOutcomes().get(outcomeId).enableBetting();
    }

    public void disableOutcomeForBetting(String eventId, String marketId, String outcomeId) {
        this.book.get(eventId).getMarkets().get(marketId).getOutcomes().get(outcomeId).disableBetting();
    }
}
