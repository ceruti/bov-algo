package lahsivjar.spring.websocket.template.util;

import lahsivjar.spring.websocket.template.model.Event;
import lahsivjar.spring.websocket.template.model.Market;
import lahsivjar.spring.websocket.template.model.Outcome;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;

public class LiveOddsUpdateUtil {

    public static Long getEventId(String rawMessage) {
        if (rawMessage.contains("}|{")) {
            WireMessage wireMessage = new WireMessage(rawMessage);
            if (wireMessage.oddsSlice.has("competitorId")) {
                String competitorId = wireMessage.oddsSlice.getString("competitorId");
                if (!competitorId.contains("-")) {
                    return null;
                }
                String eventId = competitorId.substring(0, competitorId.indexOf("-"));
                return Long.parseLong(eventId);
            }
        }
        return null;
    }

    public static boolean updateEvent(Event toUpdate, String rawMessage) {
        boolean updated = false;
        if (rawMessage.contains("}|{")) {
            WireMessage wireMessage = new WireMessage(rawMessage);
            if (toUpdate.getMarkets() != null) {
                for (Market market : toUpdate.getMarkets().values()) {
                    if (market.getOutcomes() != null
                            && wireMessage.getOutcomeId() != null
                            && wireMessage.getAmericanOdds() != null
                            && market.getOutcomes().containsKey(wireMessage.getOutcomeId())) {
                        Outcome outcomeToUpdate = market.getOutcomes().get(wireMessage.getOutcomeId());
                        outcomeToUpdate.getPrice().setAmerican(wireMessage.getAmericanOdds());
                        System.out.println(String.format("Updated odds: %s %d", wireMessage.getDescription(), wireMessage.getAmericanOdds()));
                        updated = true;
                    }
                }
            }
        }
        return updated;
    }

    @Data
    private static class WireMessage {
        private JSONObject oddsSlice;
        private JSONObject outcomeSlice;

        WireMessage(String rawMessage) {
            int pipeIndex = rawMessage.indexOf("}|{") + 1;
            String outcomeSlice = rawMessage.substring(0, pipeIndex);
            String oddsSlice = rawMessage.substring(pipeIndex+1);
            oddsSlice = StringEscapeUtils.unescapeJava(oddsSlice);
            outcomeSlice = StringEscapeUtils.unescapeJava(outcomeSlice);
            if (oddsSlice.startsWith("\"")) {
                oddsSlice = oddsSlice.substring(1, oddsSlice.length());
            }
            if (outcomeSlice.startsWith("\"")) {
                outcomeSlice = outcomeSlice.substring(1, outcomeSlice.length());
            }
            this.oddsSlice = new JSONObject(oddsSlice);
            this.outcomeSlice = new JSONObject(outcomeSlice);
        }

        Integer getAmericanOdds() {
            if (oddsSlice.has("price")) {
                JSONObject price = oddsSlice.getJSONObject("price");
                if (price.has("american")) {
                    String american = price.getString("american");
                    if (american.equalsIgnoreCase("EVEN")) {
                        return 100;
                    }
                    return Integer.parseInt(american);
                }
            }
            return null;
        }

        String getOutcomeId() {
            if (outcomeSlice != null) {
                return outcomeSlice.getString("id");
            }
            return null;
        }

        String getDescription() {
            if (oddsSlice.has("description")) {
                return oddsSlice.getString("description");
            }
            return "";
        }
    }

}
