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

    private Map<Integer, Event> book;

    public Map<Integer, Event> getBook() {
        return book;
    }

    public void setBook(Map<Integer, Event> book) {
        this.book = book;
    }

    public void addEvent(Event event) {
        book.put(event.getId(), event);
    }

    public void updateEvent(Event event) throws InvocationTargetException, IllegalAccessException {
        Event existingEvent = book.get(event.getId());
        nullAwareBeanUtilsBean.updateEvent(existingEvent, event);
    }
}
