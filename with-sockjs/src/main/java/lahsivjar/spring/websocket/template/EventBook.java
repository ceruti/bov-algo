package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventBook {

    @Autowired
    public EventBook() {
        book = new HashMap<>();
    }

    private Map<Integer, Event> book;

    public Map<Integer, Event> getBook() {
        return book;
    }

    public void setBook(Map<Integer, Event> book) {
        this.book = book;
    }

    public void addEvent(Event event) {
        // TODO: implement
    }

    public void updateEvent(Event event) {
        // TODO: implement
    }
}
