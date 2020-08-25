package lahsivjar.spring.websocket.template.util;

import lahsivjar.spring.websocket.template.model.Event;
import lahsivjar.spring.websocket.template.model.Market;
import lahsivjar.spring.websocket.template.model.Outcome;
import lahsivjar.spring.websocket.template.model.Price;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LiveOddsUpdateUtil {

    public static Long getEventId(String rawMessage) {
        if (rawMessage.contains("}|{")) {
            WireMessage wireMessage = isType1(rawMessage) ? new WireMessageType1(rawMessage) : new WireMessageType2(rawMessage);
            return wireMessage.getEventId();
        }
        return null;
    }

    private static boolean isType1(String rawMessage) {
        WireMessageType1 wireMessageType1 = new WireMessageType1(rawMessage);
        if (wireMessageType1.getOutcomeSlice().has("eventId")) {
            return false;
        }
        return true;
    }

    public static boolean updateEvent(Event toUpdate, String rawMessage) {
        boolean updated = false;
        if (rawMessage.contains("}|{")) {
            WireMessage wireMessage = isType1(rawMessage) ? new WireMessageType1(rawMessage) : new WireMessageType2(rawMessage);
            if (toUpdate.getMarkets() != null) {
                for (Market market : toUpdate.getMarkets().values()) {
                    if (market.getOutcomes() != null
                            && wireMessage.getOutcomeId() != null
                            && wireMessage.getAmericanOdds() != null
                            && market.getOutcomes().containsKey(wireMessage.getOutcomeId())) {
                        updated = updateOutcome(wireMessage, market, toUpdate.getId());
                    }
                }
            }
        }
        return updated;
    }

    private static boolean updateOutcome(WireMessage wireMessage, Market market, long eventId) {
        Outcome outcomeToUpdate = market.getOutcomes().get(wireMessage.getOutcomeId());
        wireMessage.setDescription(outcomeToUpdate.getDescription());
        Price previousPrice = outcomeToUpdate.getPrice();
        if (previousPrice.getAmerican() != wireMessage.getAmericanOdds().intValue()) {
            List<Price> previousPrices = outcomeToUpdate.getPreviousPrices();
            if (previousPrices == null) {
                previousPrices = new ArrayList<>();
            }
            Price newPrice = new Price(wireMessage.getAmericanOdds(), previousPrice.getId(), new Date());
            previousPrices.add(previousPrice);
            outcomeToUpdate.setPreviousPrices(previousPrices);
            outcomeToUpdate.setPrice(newPrice);
            System.out.println(String.format("[event %d] Updated odds: %s %d", eventId, wireMessage.getDescription(), wireMessage.getAmericanOdds()));
            return true;
        }
        return false;
    }

    private interface WireMessage {
        Integer getAmericanOdds();

        String getOutcomeId();

        String getDescription();

        Long getEventId();

        void setDescription(String newDescription);
    }

    @Data
    static class WireMessageType2 extends WireMessageType1 implements WireMessage {

        private String eventDescription;

        public WireMessageType2(String rawMessage) {
            super(rawMessage);
        }

        @Override
        public Integer getAmericanOdds() {
            return super.getAmericanOdds();
        }

        @Override
        public String getOutcomeId() {
            return oddsSlice.getString("id");
        }

        @Override
        public String getDescription() {
            return eventDescription;
        }

        @Override
        public Long getEventId() {
            return Long.parseLong(outcomeSlice.getString("eventId"));
        }

        @Override
        public void setDescription(String description) {
            this.eventDescription = description;
        }
    }

    @Data
    private static class WireMessageType1 implements WireMessage {
        protected JSONObject oddsSlice;
        protected JSONObject outcomeSlice;

        WireMessageType1(String rawMessage) {
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

        public Integer getAmericanOdds() {
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

        public String getOutcomeId() {
            if (outcomeSlice != null) {
                return outcomeSlice.getString("id");
            }
            return null;
        }

        public String getDescription() {
            if (oddsSlice.has("description")) {
                return oddsSlice.getString("description");
            }
            return "";
        }

        @Override
        public Long getEventId() {
            if (oddsSlice.has("competitorId")) {
                String competitorId = oddsSlice.getString("competitorId");
                if (!competitorId.contains("-")) {
                    return null;
                }
                String eventId = competitorId.substring(0, competitorId.indexOf("-"));
                return Long.parseLong(eventId);
            }
            return null;
        }

        @Override
        public void setDescription(String newDescription) {
            // do nothing
        }
    }

}
