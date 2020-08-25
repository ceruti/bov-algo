package integration;

import lahsivjar.spring.websocket.template.Application;
import lahsivjar.spring.websocket.template.EventRepository;
import lahsivjar.spring.websocket.template.model.Event;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class)
@AutoConfigureMockMvc
public class StrategyAnalysisIntegrationTests {



    public StrategyAnalysisIntegrationTests() {
    }

    @Autowired
    EventRepository eventRepository;

    @Test
    public void t() {
        List<Event> all = eventRepository.findAll();
        System.out.println("hiii");
    }

}
