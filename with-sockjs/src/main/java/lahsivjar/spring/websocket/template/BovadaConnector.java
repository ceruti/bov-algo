package lahsivjar.spring.websocket.template;

import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.logging.Level;

@Component
public class BovadaConnector {

    private WebDriver driver;

    public BovadaConnector() throws InterruptedException {
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

        driver.navigate().to("https://www.bovada.lv/sports");
        Thread.sleep(5000);
    }

    @Scheduled(fixedDelay = 50)
    public void funnelMessages() {
        LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
        logEntries.forEach(entry->{
            JSONObject messageJSON = new JSONObject(entry.getMessage());
            String method = messageJSON.getJSONObject("message").getString("method");
            if(method.equalsIgnoreCase("Network.webSocketFrameSent")){
                System.out.println("Message Sent: " + messageJSON.getJSONObject("message").getJSONObject("params").getJSONObject("response").getString("payloadData"));
            }else if(method.equalsIgnoreCase("Network.webSocketFrameReceived")){
                System.out.println("Message Received: " + messageJSON.getJSONObject("message").getJSONObject("params").getJSONObject("response").getString("payloadData"));
            }
        });
    }

}
