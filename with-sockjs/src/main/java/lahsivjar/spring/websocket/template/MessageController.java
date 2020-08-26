package lahsivjar.spring.websocket.template;

import java.util.Map;

import lahsivjar.spring.websocket.template.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
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
        return message;
    }

    @RequestMapping("/events")
    public Map<Long, Event> getEvents() {
        return this.eventBook.getLiveEvents();
    }

//    @RequestMapping("/history")
//    public List<Map<String, String>> getChatHistory() {
//        return chatHistoryDao.get();
//    }
}
