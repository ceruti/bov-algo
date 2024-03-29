package com.ceruti.bov;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.util.EventParseUtil;
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

    private SharedExecutorService sharedExecutorService;

    private EventBook eventBook;

    @Autowired
    public EventInitializerService(EventBook eventBook, SharedExecutorService sharedExecutorService) {
        this.eventBook = eventBook;
        restTemplate = new RestTemplate();
        this.sharedExecutorService = sharedExecutorService;
    }

    public void syncEventsAsync(String url) {
        sharedExecutorService.getExecutorService().submit(() -> {
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
                JSONObject rawEvent = events.getJSONObject(j);
                Long id = Long.parseLong(rawEvent.getString("id"));
                Event parsedEvent = EventParseUtil.parseEvent(rawEvent);
                if (eventBook.getBook().containsKey(id)) {
                    eventBook.getBook().get(id).getRawEventSummaries().add(rawEvent.toString());
                    eventBook.updateEvent(parsedEvent);
                } else {
                    eventBook.addEvent(parsedEvent);
                }
            }
        }
    }

}
