package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventBook {

    private NullAwareBeanUtilsBean nullAwareBeanUtilsBean;

    @Autowired
    public EventBook(NullAwareBeanUtilsBean nullAwareBeanUtilsBean) {
        this.nullAwareBeanUtilsBean = nullAwareBeanUtilsBean;
        book = new HashMap<>();
    }

    private Map<Long, Event> book;

    public Map<Long, Event> getBook() {
        return book;
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
}