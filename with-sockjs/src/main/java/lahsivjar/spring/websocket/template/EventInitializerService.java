package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Event;
import lahsivjar.spring.websocket.template.util.EventParseUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EventInitializerService {

    private RestTemplate restTemplate;

    private ExecutorService executorService;

    private EventBook eventBook;

    @Autowired
    public EventInitializerService(EventBook eventBook) {
        this.eventBook = eventBook;
        restTemplate = new RestTemplate();
        executorService = Executors.newFixedThreadPool(5);
    }

    public void syncEventsAsync(String url) {
        executorService.submit(() -> {
            try {
                syncEvent(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void syncEvent(String url) throws InvocationTargetException, IllegalAccessException {
        String eventsJSON = restTemplate.getForObject(url, String.class);
        JSONArray parentJSON = new JSONArray(eventsJSON);
        for (int i=0; i<parentJSON.length(); i++) {
            JSONObject parentEl = parentJSON.getJSONObject(i);
            JSONArray events = parentEl.getJSONArray("events");
            for (int j=0; j<events.length(); j++) {
                JSONObject event = events.getJSONObject(j);
                Integer id = Integer.parseInt(event.getString("id"));
                Event parsedEvent = EventParseUtil.parseEvent(event);
                if (eventBook.getBook().containsKey(id)) {
                    eventBook.updateEvent(parsedEvent);
                } else {
                    eventBook.addEvent(parsedEvent);
                }
            }
        }
        System.out.println("here");
    }

}
