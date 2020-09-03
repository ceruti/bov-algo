package com.ceruti.bov;

import com.ceruti.bov.model.*;
import com.ceruti.bov.strategy.Strategy;
import com.ceruti.bov.util.ActiveProfileService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class LiveFeedUpdateService {

    public static final Strategy BETTING_STRATEGY = Strategy.AGGRESSIVE; // TODO: inject this via properties/VM args?
    private final ActiveProfileService activeProfileService;
    private BettingFacilitatorService bettingFacilitatorService;
    private EventBook eventBook;

    @Autowired
    public LiveFeedUpdateService(BettingFacilitatorService bettingFacilitatorService, EventBook eventBook, ActiveProfileService activeProfileService) {
        this.bettingFacilitatorService = bettingFacilitatorService;
        this.eventBook = eventBook;
        this.activeProfileService = activeProfileService;
    }

    public Collection<Long> getEventIds(String rawMessage) {
        if (rawMessage.contains("}|{")) {
            WireMessage wireMessage = getWireMessage(rawMessage);
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
            WireMessage wireMessage = getWireMessage(rawMessage);
            if (!wireMessage.getType().equalsIgnoreCase("outcome")) {
                return false;
            }
            if (toUpdate.getMarkets() != null) {
                for (Market market : toUpdate.getMarkets().values()) {
                    if (market.getOutcomes() != null
                            && wireMessage.getOutcomeId() != null
                            && wireMessage.getAmericanOdds() != null
                            && market.getOutcomes().containsKey(wireMessage.getOutcomeId())) {
                        updated = updateOutcome(wireMessage, market, toUpdate.getId(), toUpdate);
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

    public WireMessage getWireMessage(String rawMessage) {
        return isType1(rawMessage) ? new WireMessageType1(rawMessage) : new WireMessageType2(rawMessage);
    }

    private boolean updateEvent(Event toUpdate, JSONObject eventUpdate) {
        if (eventUpdate.has("sport")) {
            toUpdate.setSport(eventUpdate.getString("sport"));
        }
        if (eventUpdate.has("latestScore")) {
            if ("TENNIS".equalsIgnoreCase(EventBook.getEquivalentKey(eventUpdate.getString("sport")))) {
                updateTennisScore(eventUpdate, toUpdate);
            } else {
                JSONObject latestScore = eventUpdate.getJSONObject("latestScore");
                if (latestScore.getString("visitor") != null) {
                    toUpdate.setVisitorScore(latestScore.getString("visitor"));
                }
                if (latestScore.getString("home") != null) {
                    toUpdate.setHomeScore(latestScore.getString("home"));
                }
            }
        }
        if (eventUpdate.has("currentPeriodScore")) {
            JSONObject currentPeriodScore = eventUpdate.getJSONObject("currentPeriodScore");
            try {
                toUpdate.setCurrentPeriodHomeScore(currentPeriodScore.getInt("home"));
                toUpdate.setCurrentPeriodVisitorScore(currentPeriodScore.getInt("visitor"));
            } catch (Exception e) { // have only seen this as an int but it could be a string?
                e.printStackTrace();
            }
        }
        if (eventUpdate.has("gameStatus")) {
            String gameStatus = eventUpdate.getString("gameStatus");
            toUpdate.setGameStatus(gameStatus);
            if (gameStatus.equalsIgnoreCase("GAME_END")) {
                updateWinningOutcome(toUpdate);
            }
        }
        updateClock(toUpdate, eventUpdate);
        if (!activeProfileService.isTestMode()) {
            log.info(String.format("[event %d] Updated event: %s", toUpdate.getId(), toUpdate.getDescription()));
        }
        return true;
    }

    private static void updateTennisScore(JSONObject eventUpdate, Event toUpdate) {
        if (eventUpdate.has("sportDetails")) {
            JSONObject sportDetails = eventUpdate.getJSONObject("sportDetails");
            if (sportDetails.has("tennis")) {
                JSONObject tennisDetails = sportDetails.getJSONObject("tennis");
                if (tennisDetails.has("sets")) {
                    JSONObject sets = tennisDetails.getJSONObject("sets");
                    toUpdate.setHomeScore(Integer.toString(sets.getInt("home")));
                    toUpdate.setVisitorScore(Integer.toString(sets.getInt("visitor")));
                }
            }
        }
    }

    private static void updateWinningOutcome(Event toUpdate) {
        try {
            Optional<Market> moneyline = toUpdate.getMarkets().values().stream()
                    .filter(market -> market.getDescription().equalsIgnoreCase("Moneyline"))
                    .findFirst();
            if (!moneyline.isPresent()) {
                return;
            }
            Market moneylineMarket = moneyline.get();
            boolean homeWins = Integer.parseInt(toUpdate.getHomeScore()) > Integer.parseInt(toUpdate.getVisitorScore());
            Competitor winningCompetitor = toUpdate.getCompetitors().values().stream()
                    .filter(competitor -> homeWins == competitor.isHome())
                    .findFirst().get();
            Outcome winningOutcome = moneylineMarket.getOutcomes().values().stream()
                    .filter(outcome -> outcome.getCompetitorId().equals(winningCompetitor.getId()))
                    .findFirst().get();
            if (moneylineMarket.getBettingSession() != null) {
                moneylineMarket.getBettingSession().setWinningOutcomeId(winningOutcome.getId());
            }
        } catch (Exception e) {
            log.error("Unable to identify winner");
            e.printStackTrace();
        }
    }

    private static void updateClock(Event toUpdate, JSONObject eventUpdate) {
        // clock
        JSONObject clockUpdate = eventUpdate.getJSONObject("clock");
        if (clockUpdate != null) {
            Clock clock = new Clock();
            if (clockUpdate.has("period")) {
                clock.setPeriod(clockUpdate.getString("period"));
            }
            if (clockUpdate.has("periodNumber")) {
                clock.setPeriodNumber(clockUpdate.getInt("periodNumber"));
            }
            if (clockUpdate.has("gameTime")) {
                clock.setGameTime(clockUpdate.getString("gameTime"));
            }
            if (clockUpdate.has("isTicking")) {
                clock.setTicking(clockUpdate.getBoolean("isTicking"));
            }
            if (clockUpdate.has("numberOfPeriods")) {
                clock.setNumberOfPeriods(clockUpdate.getInt("numberOfPeriods"));
            }
            if (clockUpdate.has("direction")) {
                clock.setDirection(clockUpdate.getString("direction"));
            }
            if (clockUpdate.has("relativeGameTimeInSeconds")) {
                clock.setRelativeGameTimeInSeconds(clockUpdate.getInt("relativeGameTimeInSeconds"));
            }
            toUpdate.setClock(clock);
        }
    }

    private boolean updateOutcome(WireMessage wireMessage, Market market, long eventId, Event toUpdate) {
        Outcome outcomeToUpdate = market.getOutcomes().get(wireMessage.getOutcomeId());
        wireMessage.setDescription(outcomeToUpdate.getDescription());
        Price previousPrice = outcomeToUpdate.getPrice();
        Price newPrice = new Price(wireMessage.getAmericanOdds(), wireMessage.getNewPriceId(), toUpdate.getClock(), toUpdate.getHomeScore(), toUpdate.getVisitorScore(), toUpdate.getCurrentPeriodHomeScore(), toUpdate.getCurrentPeriodVisitorScore());
        addToPreviousPrices(outcomeToUpdate, previousPrice);
        outcomeToUpdate.setPrice(newPrice);
        if (previousPrice == null || previousPrice.getAmerican() != wireMessage.getAmericanOdds().intValue()) {
            if (!activeProfileService.isTestMode()) {
                log.info(String.format("[event %d] Updated odds: %s/%s -> %s %d",
                        eventId,
                        toUpdate.getDescription(),
                        market.getDescription(),
                        wireMessage.getDescription(),
                        wireMessage.getAmericanOdds()));
            }
            faciliatePotentialBet(eventId, newPrice, outcomeToUpdate, market);
            return true;
        }
        return false;
    }

    private void addToPreviousPrices(Outcome outcomeToUpdate, Price previousPrice) {
        List<Price> previousPrices = outcomeToUpdate.getPreviousPrices();
        if (previousPrices == null) {
            previousPrices = new ArrayList<>();
        }
        if (previousPrice != null) {
            previousPrices.add(previousPrice);
            outcomeToUpdate.setPreviousPrices(previousPrices);
        }
    }

    private void faciliatePotentialBet(long eventId, Price price, Outcome outcome, Market market) {
        Event event = eventBook.getBook().get(eventId);
        if (market.getDescription().equalsIgnoreCase("MoneyLine") && market.getOutcomes().size() == 2) {
            Outcome opposingOutcome = market.getOutcomes().values()
                    .stream()
                    .filter(otherOutcome -> !otherOutcome.getId().equals(outcome.getId()))
                    .findFirst()
                    .get();
            bettingFacilitatorService.updateBettingSession( // TODO: switch back to async? --> don't see a reason to do async, because we are not blocking the bovadaConnector thread
                    event,
                    market,
                    outcome,
                    opposingOutcome,
                    price,
                    BETTING_STRATEGY
            );
        }
    }

    public interface WireMessage {
        Integer getAmericanOdds();

        String getOutcomeId();

        String getDescription();

        Long getEventId();

        void setDescription(String newDescription);

        String getNewPriceId();

        String getType();

    }

    @Data
    public static class WireMessageType2 extends WireMessageType1 implements WireMessage {

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
    public static class WireMessageType1 implements WireMessage {
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
                    return Integer.parseInt(american.replace("+", ""));
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
        public String getNewPriceId() {
            return oddsSlice.has("price") ? oddsSlice.getJSONObject("price").getString("id") : null;
        }

        @Override
        public String getType() {
            return outcomeSlice.getString("type");
        }
    }

}
