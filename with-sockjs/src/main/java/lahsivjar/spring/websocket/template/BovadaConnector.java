package lahsivjar.spring.websocket.template;

import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Component
public class BovadaConnector {

    private WebDriver driver;

    private SimpMessagingTemplate template;

    private ChatHistoryDao chatHistoryDao;

    private EventInitializerService eventInitializerService;

    private LiveOddsUpdateService liveOddsUpdateService;

    private RestTemplate restTemplate = new RestTemplate();

    private EventBook eventBook;

    @Autowired
    public BovadaConnector(SimpMessagingTemplate template,
                           ChatHistoryDao chatHistoryDao,
                           EventInitializerService eventInitializerService,
                           LiveOddsUpdateService liveOddsUpdateService,
                           EventBook eventBook) throws InterruptedException {
        this.template = template;
        this.chatHistoryDao = chatHistoryDao;
        this.eventInitializerService = eventInitializerService;
        this.liveOddsUpdateService = liveOddsUpdateService;
        this.eventBook = eventBook;
        System.setProperty("webdriver.chrome.driver", "/Users/marc.ceruti/drivers/chromedriver");
        LoggingPreferences loggingprefs = new LoggingPreferences();
        loggingprefs.enable(LogType.PERFORMANCE, Level.ALL);

        DesiredCapabilities cap = new DesiredCapabilities().chrome();
//        cap.setCapability(CapabilityType.LOGGING_PREFS, loggingprefs);
//        ChromeOptions options = new ChromeOptions();
//        LoggingPreferences logPrefs = new LoggingPreferences();
//        logPrefs.enable( LogType.PERFORMANCE, Level.ALL );
        cap.setCapability( "goog:loggingPrefs", loggingprefs );

        driver = new ChromeDriver(cap);

        driver.navigate().to("https://www.bovada.lv/sports/live");
        Thread.sleep(5000);
    }

    @Scheduled(fixedDelay = 50)
    public void funnelMessages() {
        if (!eventBook.isEnableUpdates()) {
            return;
        }
        LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
        logEntries.forEach(entry->{
            JSONObject messageJSON = new JSONObject(entry.getMessage());
            JSONObject message = messageJSON.getJSONObject("message");
            String method = message.getString("method");
            try {
                JSONObject params = message.getJSONObject("params");
                JSONObject response = params.getJSONObject("response");
                if (method.equalsIgnoreCase("Network.responseReceived")
                    && response.getString("url").contains("coupon")
                ) {
                    eventInitializerService.syncEventsAsync(response.getString("url"));
                }
                else if(method.equalsIgnoreCase("Network.webSocketFrameSent")){
//                    System.out.println("Message Sent: " + payload);
                }else if(method.equalsIgnoreCase("Network.webSocketFrameReceived")){
                    String payload = response.getString("payloadData");
                    liveOddsUpdateService.updateEventBook(payload);
//                    System.out.println("Message Received: " + payload);
//                    this.template.convertAndSend("/topic/all", payload);
                    Map<String, String> _message = new HashMap<>();
                    _message.put("author", "bov-boy");
                    _message.put("authorId", "GZ0Ut7zC4mKHfeEmQx0ZnloZxIH8J4Lh");
                    _message.put("message", payload);
                    _message.put("timestamp", Long.toString(System.currentTimeMillis()));
//                    chatHistoryDao.save(message); TODO: uncomment this?

                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
        });
    }

}
