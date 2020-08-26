package lahsivjar.spring.websocket.template;

import java.util.Map;

import lahsivjar.spring.websocket.template.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private EventBook eventBook;

    @Autowired
    public MessageController(EventBook eventBook) {
        this.eventBook = eventBook;
    }

    /*
     * This MessageMapping annotated method will be handled by
     * SimpAnnotationMethodMessageHandler and after that the Message will be
     * forwarded to Broker channel to be forwarded to the client via WebSocket
     */
    @MessageMapping("/all")
    @SendTo("/topic/all")
    public Map<String, String> post(@Payload Map<String, String> message) {
//        message.put("timestamp", Long.toString(System.currentTimeMillis()));
//        chatHistoryDao.save(message);
        // tODO: something with messages from the front end??
        return message;
    }

    @RequestMapping("/events")
    public Map<String, Map<Long, Event>> getEvents() {
        return this.eventBook.getLiveEvents();
    }

    // NOTE: these requests are served over HTTPS instead of WebSocket because WebSockets are not as reliable and these commands are critical, even if latency is higher

    @RequestMapping(value = "/events/{eventId}/enable", method = RequestMethod.PUT)
    public void enableEventForBetting(@PathVariable(value="eventId") String eventId) {
        this.eventBook.enableEventForBetting(eventId);
    }

    @RequestMapping(value = "/events/{eventId}/disable", method = RequestMethod.PUT)
    public void disableEventForBetting(@PathVariable(value="eventId") String eventId) {
        this.eventBook.disableEventForBetting(eventId);
    }

    @RequestMapping(value = "/events/{eventId}/markets/{marketId}/outcomes/{outcomeId}/enable", method = RequestMethod.PUT)
    public void enableOutcomeForBetting(@PathVariable(value="eventId") String eventId,
                                        @PathVariable(value="marketId") String marketId,
                                        @PathVariable(value="outcomeId") String outcomeId) {
        this.eventBook.enableOutcomeForBetting(eventId, marketId, outcomeId);
    }

    @RequestMapping(value = "/events/{eventId}/markets/{marketId}/outcomes/{outcomeId}/disable", method = RequestMethod.PUT)
    public void disableOutcomeForBetting(@PathVariable(value="eventId") String eventId,
                                         @PathVariable(value="marketId") String marketId,
                                         @PathVariable(value="outcomeId") String outcomeId) {
        this.eventBook.disableOutcomeForBetting(eventId, marketId, outcomeId);
    }

//    @RequestMapping("/history")
//    public List<Map<String, String>> getChatHistory() {
//        return chatHistoryDao.get();
//    }
}
