package lahsivjar.spring.websocket.template.util;

import lahsivjar.spring.websocket.template.EventBook;
import lahsivjar.spring.websocket.template.LiveOddsUpdateService;
import lahsivjar.spring.websocket.template.model.*;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LiveFeedUpdateService {

    private BettingFacilitatorService bettingFacilitatorService;
    private EventBook eventBook;

    @Autowired
    public LiveFeedUpdateService(BettingFacilitatorService bettingFacilitatorService, EventBook eventBook) {
        this.bettingFacilitatorService = bettingFacilitatorService;
        this.eventBook = eventBook;
    }

    public static Collection<Long> getEventIds(String rawMessage) {
        if (rawMessage.contains("}|{")) {
            WireMessage wireMessage = isType1(rawMessage) ? new WireMessageType1(rawMessage) : new WireMessageType2(rawMessage);
            if (!wireMessage.getType().equalsIgnoreCase("outcome")) {
                return null;
            }
            return Collections.singletonList(wireMessage.getEventId());
        } else {
            JSONArray eventUpdates = new JSONArray(rawMessage);
            List<Long> eventIds = new ArrayList<>();
            for (int i=0; i<eventUpdates.length(); i++) {
                JSONObject eventUpdate = eventUpdates.getJSONObject(i);
                eventIds.add(eventUpdate.getLong("eventId"));
            }
            return eventIds;
        }
    }

    private static boolean isType1(String rawMessage) {
        WireMessageType1 wireMessageType1 = new WireMessageType1(rawMessage);
        if (wireMessageType1.getOutcomeSlice().has("eventId")) {
            return false;
        }
        return true;
    }

    public boolean updateEvent(Event toUpdate, String rawMessage) {
        boolean updated = false;
        if (rawMessage.contains("}|{")) {
            WireMessage wireMessage = isType1(rawMessage) ? new WireMessageType1(rawMessage) : new WireMessageType2(rawMessage);
            if (!wireMessage.getType().equalsIgnoreCase("outcome")) {
                return false;
            }
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
        } else {
            JSONArray eventUpdates = new JSONArray(rawMessage);
            for (int i=0; i<eventUpdates.length(); i++) {
                JSONObject eventUpdate = eventUpdates.getJSONObject(i);
                if (eventUpdate.getLong("eventId") == toUpdate.getId()) {
                    return updateEvent(toUpdate, eventUpdate);
                }
            }
        }
        return updated;
    }

    private static boolean updateEvent(Event toUpdate, JSONObject eventUpdate) {
        if (eventUpdate.has("sport")) {
            toUpdate.setSport(eventUpdate.getString("sport"));
        }
        if (eventUpdate.has("latestScore")) {
            JSONObject latestScore = eventUpdate.getJSONObject("latestScore");
            toUpdate.setVisitorScore(latestScore.getString("visitor"));
            toUpdate.setHomeScore(latestScore.getString("home"));
        }
        if (eventUpdate.has("gameStatus")) {
            toUpdate.setGameStatus(eventUpdate.getString("gameStatus"));
        }
        updateClock(toUpdate, eventUpdate);
        System.out.println(String.format("[event %d] Updated event: %s", toUpdate.getId(), toUpdate.getDescription()));
        return true;
    }

    private static void updateClock(Event toUpdate, JSONObject eventUpdate) {
        // clock
        JSONObject clockUpdate = eventUpdate.getJSONObject("clock");
        if (clockUpdate != null) {
            Clock clock = new Clock();
            clock.setPeriod(clockUpdate.getString("period"));
            clock.setPeriodNumber(clockUpdate.getInt("periodNumber"));
            clock.setGameTime(clockUpdate.getString("gameTime"));
            clock.setTicking(clockUpdate.getBoolean("isTicking"));
            toUpdate.setClock(clock);
        }
    }

    private boolean updateOutcome(WireMessage wireMessage, Market market, long eventId) {
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
            faciliatePotentialBet(eventId, newPrice, outcomeToUpdate, market);
            return true;
        }
        return false;
    }

    private void faciliatePotentialBet(long eventId, Price price, Outcome outcome, Market market) {
        Event event = eventBook.getBook().get(eventId);
        if (market.getDescription().equalsIgnoreCase("MoneyLine") && market.getOutcomes().size() == 2) {
            Outcome opposingOutcome = market.getOutcomes().values()
                    .stream()
                    .filter(otherOutcome -> !otherOutcome.getId().equals(outcome.getId()))
                    .findFirst()
                    .get();
            bettingFacilitatorService.updateBettingSessionAsync(
                    event,
                    market,
                    outcome,
                    opposingOutcome,
                    price,
                    Strategy.BASIC
            );
        }
    }

    private interface WireMessage {
        Integer getAmericanOdds();

        String getOutcomeId();

        String getDescription();

        Long getEventId();

        void setDescription(String newDescription);

        String getType();
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

        @Override
        public String getType() {
            return outcomeSlice.getString("type");
        }
    }

}