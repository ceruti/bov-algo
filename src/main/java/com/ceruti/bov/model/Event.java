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
    private long id;
    private String homeScore;
    private String visitorScore;
    private int currentPeriodHomeScore;
    private int currentPeriodVisitorScore;
//    private Date vendorLastUpdated;
    private Clock clock;
    private String sport;
    private Map<String, Competitor> competitors;
    private String description;
    private String notes;
    private boolean live;
    private int competitionId;
    private Map<String, Market> markets;
    private String gameStatus;
    private Date lastUpdated;
    private Date createdAt;
    private String version = "v2.0";

    @JsonIgnore
    List<String> rawWireMessages = new ArrayList<>();
    @JsonIgnore
    List<JSONObject> rawEventSummaries = new ArrayList<>();

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

    private boolean bettingEnabled = true; // TODO: flip this back

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
