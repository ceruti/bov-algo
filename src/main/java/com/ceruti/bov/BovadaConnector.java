package com.ceruti.bov;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Component
@Profile("!test")
public class BovadaConnector {

    private static final int REFRESH_PAGE_INTERVAL = 1000 * 60 * 10;

    private BetPlacingService betPlacingService;

    private WebDriver driver;

    private SimpMessagingTemplate template;

    private EventInitializerService eventInitializerService;

    private LiveOddsUpdateService liveOddsUpdateService;

    private RestTemplate restTemplate = new RestTemplate();

    private EventBook eventBook;

    private DateTime lastMessagedReceived = new DateTime();

    private DateTime lastRefresh = new DateTime();

    @Autowired
    public BovadaConnector(SimpMessagingTemplate template,
                           EventInitializerService eventInitializerService,
                           LiveOddsUpdateService liveOddsUpdateService,
                           EventBook eventBook,
                           BetPlacingService betPlacingService) throws InterruptedException {
        this.template = template;
        this.eventInitializerService = eventInitializerService;
        this.liveOddsUpdateService = liveOddsUpdateService;
        this.eventBook = eventBook;
        this.betPlacingService = betPlacingService;
        System.setProperty("webdriver.chrome.driver", "/Users/marc.ceruti/drivers/chromedriver");
        LoggingPreferences loggingprefs = new LoggingPreferences();
        loggingprefs.enable(LogType.PERFORMANCE, Level.ALL);

        DesiredCapabilities cap = new DesiredCapabilities().chrome();
        cap.setCapability( "goog:loggingPrefs", loggingprefs );

        driver = new ChromeDriver(cap);

        driver.navigate().to("https://www.bovada.lv/sports/live");
        Thread.sleep(5000);
    }

    // every three minutes, refresh the page to find new events.
    // sometimes odds feeds will stop too, and this helps with that
    @Scheduled(fixedDelay = 1000*60*3)
    public void refresh() {
        if (betPlacingService.getToken() != null) {
            driver.navigate().to("https://www.bovada.lv/sports/live");
            lastRefresh = new DateTime();
        }
    }

    @Scheduled(fixedDelay = 50)
    public void funnelMessages() {
        if (!eventBook.isEnableUpdates()) {
            return;
        }
        if (betPlacingService.getToken() == null) {
            return;
        }
        if (lastMessagedReceived.isBefore(new DateTime().minusSeconds(20))
            && lastRefresh.isBefore(new DateTime().minusSeconds(20))) {
            System.err.println("No messages received in 20 seconds. Refreshing...");
            refresh();
        }
        LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
        logEntries.forEach(entry->{
            JSONObject messageJSON = new JSONObject(entry.getMessage());
            JSONObject message = messageJSON.getJSONObject("message");
            String method = message.getString("method");
            JSONObject params = message.getJSONObject("params");
            if (method.equalsIgnoreCase("Network.requestWillBeSent")) {
                try {
                    String token = params.getJSONObject("request").getJSONObject("headers").getString("Authorization");
                    betPlacingService.setToken(token);
                } catch (Exception e) {
                    // do nothing
                }
            } else if (betPlacingService.getToken() != null) {
                try {
                    JSONObject response = params.getJSONObject("response");
                    if (method.equalsIgnoreCase("Network.responseReceived")
                            && response.getString("url").contains("coupon")
                    ) {
                        eventInitializerService.syncEventsAsync(response.getString("url"));
                    }
                    else if(method.equalsIgnoreCase("Network.webSocketFrameSent")){
                        // do nothing for now
                    }else if(method.equalsIgnoreCase("Network.webSocketFrameReceived")){
                        String payload = response.getString("payloadData");
                        liveOddsUpdateService.updateEventBookAsync(payload);
                        lastMessagedReceived = new DateTime();
                    }
                } catch (Exception e) {
//                e.printStackTrace();
                }
            }
        });
    }

}
