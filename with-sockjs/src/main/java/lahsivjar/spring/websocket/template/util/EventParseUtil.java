package lahsivjar.spring.websocket.template.util;

import lahsivjar.spring.websocket.template.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventParseUtil {


    public static Event parseEvent(JSONObject eventJSONObject) {
        Event event = new Event();
        event.setCreatedAt(new Date());
        event.setLastUpdated(new Date());
        setBaseFields(eventJSONObject, event);
        setCompetitors(eventJSONObject, event);
        setMarkets(eventJSONObject, event);
        return event;
    }

    protected static void setMarkets(JSONObject eventJSONObject, Event event) {
        JSONArray displayGroups = eventJSONObject.getJSONArray("displayGroups");
        Map<String, Market> markets = new HashMap<>();
        for (int i=0; i<displayGroups.length(); i++) {
            JSONObject displayGroup = displayGroups.getJSONObject(i);
            JSONArray marketsJSON = displayGroup.getJSONArray("markets");
            for (int j=0; j<marketsJSON.length(); j++) {
                JSONObject marketJSONObj = marketsJSON.getJSONObject(j);
                Market market = new Market();
                setBaseFields(marketJSONObj, market);
                setOutcomes(marketJSONObj, market);
                markets.put(market.getId(), market);
            }

        }
        event.setMarkets(markets);
    }

    protected static void setBaseFields(JSONObject marketJSONObj, Market market) {
        market.setDescription(marketJSONObj.getString("description"));
        market.setDescriptionKey(marketJSONObj.getString("descriptionKey"));
        market.setId(marketJSONObj.getString("id"));
        market.setKey(marketJSONObj.getString("key"));
        market.setMarketTypeId(marketJSONObj.getString("marketTypeId"));
        market.setNotes(marketJSONObj.getString("notes"));
        market.setDescription(marketJSONObj.getString("description"));
    }

    protected static void setOutcomes(JSONObject marketJSONObj, Market market) {
        Map<String, Outcome> outcomes = new HashMap<>();
        JSONArray outcomeArrJSON = marketJSONObj.getJSONArray("outcomes");
        boolean twoOutcomes = outcomeArrJSON.length() == 2;
        for (int k=0; k<outcomeArrJSON.length(); k++) {
            JSONObject outcomeJSON = outcomeArrJSON.getJSONObject(k);
            Outcome outcome = new Outcome();
            if (twoOutcomes) {
                int opposingOutcomeIndex = k==0 ? 1 : 0;
                String opposingOutcomeId = outcomeArrJSON.getJSONObject(opposingOutcomeIndex).getString("id");
                outcome.setOpposingOutcomeId(opposingOutcomeId);
            }
            setBaseFields(outcomeJSON, outcome);
            setPrice(outcomeJSON, outcome);
            outcomes.put(outcome.getId(), outcome);
        }
        market.setOutcomes(outcomes);
    }

    protected static void setBaseFields(JSONObject outcomeJSON, Outcome outcome) {
        if (outcomeJSON.has("competitorId")) {
            outcome.setCompetitorId(outcomeJSON.getString("competitorId"));
        }
        outcome.setDescription(outcomeJSON.getString("description"));
        outcome.setId(outcomeJSON.getString("id"));
        outcome.setStatus(outcomeJSON.getString("status"));
        outcome.setType(outcomeJSON.getString("type"));
    }

    protected static void setPrice(JSONObject outcomeJSON, Outcome outcome) {
        Price price = new Price();
        JSONObject jsonPrice = outcomeJSON.getJSONObject("price");
        String american = jsonPrice.getString("american");
        if (american.equalsIgnoreCase("EVEN")) {
            price.setAmerican(100);
        } else {
            price.setAmerican(Integer.parseInt(american));
        }
        price.setId(jsonPrice.getString("id"));
        outcome.setPrice(price);
    }

    protected static void setBaseFields(JSONObject eventJSONObject, Event event) {
        String id = eventJSONObject.getString("id");
        event.setId(Integer.parseInt(id));
        event.setDescription(eventJSONObject.getString("description"));
        event.setSport(eventJSONObject.getString("sport"));
        event.setNotes(eventJSONObject.getString("notes"));
        event.setLive(eventJSONObject.getBoolean("live"));
        event.setCompetitionId(Integer.parseInt(eventJSONObject.getString("competitionId")));
    }

    protected static void setCompetitors(JSONObject eventJSONObject, Event event) {
        JSONArray competitorsArr = eventJSONObject.getJSONArray("competitors");
        Map<String, Competitor> competitors = new HashMap<>();
        for (int i=0; i<competitorsArr.length(); i++) {
            JSONObject competitorJSON = competitorsArr.getJSONObject(i);
            Competitor competitor = new Competitor();
            competitor.setId(competitorJSON.getString("id"));
            competitor.setName(competitorJSON.getString("name"));
            competitor.setHome(competitorJSON.getBoolean("home"));
            competitors.put(competitor.getId(), competitor);
        }
        event.setCompetitors(competitors);
    }

}
