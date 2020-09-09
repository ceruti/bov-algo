package com.ceruti.bov.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ceruti.bov.util.SportLogicUtil;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class Event {

    @Id
    protected long id;
    protected String homeScore;
    protected String visitorScore;
    protected int currentPeriodHomeScore;
    protected int currentPeriodVisitorScore;
//    protected Date vendorLastUpdated;
    protected Clock clock;
    protected String sport;
    protected Map<String, Competitor> competitors;
    protected String description;
    protected String notes;
    protected boolean live;
    protected int competitionId;
    protected Map<String, Market> markets;
    protected String gameStatus;
    protected Date lastUpdated;
    protected Date createdAt;
    protected String version = "v2.4";

    @JsonIgnore
    List<String> rawWireMessages = new ArrayList<>();
    @JsonIgnore
    List<String> rawEventSummaries = new ArrayList<>();

    public Event() {

    }

    @JsonProperty("isEndingSoon")
    public boolean isEndingSoon() {
        try {
            return SportLogicUtil.isEndingSoon(this);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @JsonProperty("startedRecently")
    public boolean startedRecently() {
        try {
            return SportLogicUtil.startedRecently(this);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // note: this is just on initialization: can be flipped in the UI in a LIVE setting
    protected boolean bettingEnabled = System.getProperty("spring.profiles.active") != null
            && (System.getProperty("spring.profiles.active").contains("test") || System.getProperty("spring.profiles.active").contains("enableAutoBetting"));

    public void enableBetting() {
        this.bettingEnabled = true;
    }

    public void disableBetting() {
        this.bettingEnabled = false;
    }

    public void markUpdated() {
        lastUpdated = new Date();
    }

    public boolean hasAnotherBettingSession(String besidesMarketId) {
        return markets.values().stream()
                .anyMatch(market -> !market.getId().equals(besidesMarketId) && market.getBettingSession() != null);
    }

}
