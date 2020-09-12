package com.ceruti.bov;

import com.ceruti.bov.model.*;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NullAwareBeanUtilsBean extends BeanUtilsBean {

    private static final Set<Class<?>> primitiveTypes = new HashSet<Class<?>>(
            Arrays.asList(Boolean.class,
                    Character.class,
                    Byte.class,
                    Short.class,
                    Integer.class,
                    Long.class,
                    Float.class,
                    Double.class,
                    Void.class,
                    String.class,
                    Date.class));

    @Override
    public void copyProperty(Object dest, String name, Object value)
            throws IllegalAccessException, InvocationTargetException {
        if (value == null)
            return;

        if (primitiveTypes.contains(value.getClass())) {
            super.copyProperty(dest, name, value);
        } else {
            // don't try to handle non primitives...
        }
    }

    public Event cloneEvent(Event source) throws Exception {
        Event newEvent = new Event();
        this.copyProperties(newEvent, source);
        if (source.getClock() != null) {
            newEvent.setClock(source.getClock().clone());
        }
        if (source.getCompetitors() != null) {
            cloneCompetitors(source, newEvent);
        }
        if (source.getMarkets() != null) {
            cloneMarkets(source, newEvent);
        }
        if (source.getRawWireMessages() != null) {
            this.cloneRawWireMessages(source, newEvent);
        }
        if (source.getRawEventSummaries() != null) {
            this.cloneRawEventSummaries(source, newEvent);
        }
        return newEvent;
    }

    private void cloneRawEventSummaries(Event source, Event newEvent) {
        List<String> eventSummaries = source.getRawEventSummaries().stream().map(message -> message).collect(Collectors.toList());
        newEvent.setRawEventSummaries(eventSummaries);
    }

    private void cloneRawWireMessages(Event source, Event newEvent) {
        List<String> wireMessages = source.getRawWireMessages().stream().map(message -> message).collect(Collectors.toList());
        newEvent.setRawWireMessages(wireMessages);
    }

    private void cloneMarkets(Event source, Event newEvent) throws Exception {
        Map<String, Market> markets = new HashMap<>();
        for (String marketId : source.getMarkets().keySet()) {
            Market market = this.cloneMarket(source.getMarkets().get(marketId));
            markets.put(marketId, market);
        }
        newEvent.setMarkets(markets);
    }

    private Market cloneMarket(Market source) throws Exception {
        Market result = new Market();
        this.copyProperties(result, source);
        result.setOutcomes(this.cloneOutcomes(source));
        return result;
    }

    private Map<String, Outcome> cloneOutcomes(Market source) throws Exception {
        Map<String, Outcome> outcomes = new HashMap<>();
        for (String outcomeId : source.getOutcomes().keySet()) {
            Outcome outcome = this.cloneOutcome(source.getOutcomes().get(outcomeId));
            outcomes.put(outcomeId, outcome);
        }
        return outcomes;
    }

    private Outcome cloneOutcome(Outcome source) throws Exception {
        Outcome result = new Outcome();
        this.copyProperties(result, source);
        result.setPrice(source.getPrice().clone());
        if (source.getPreviousPrices() != null) {
            List<Price> previousPrices = source.getPreviousPrices().stream().map(Price::clone).collect(Collectors.toList());
            result.setPreviousPrices(previousPrices);
        }
        return source;
    }

    protected void cloneCompetitors(Event source, Event newEvent) {
        Map<String, Competitor> competitors = new HashMap<>();
        for (String competitorId : source.getCompetitors().keySet()) {
            competitors.put(competitorId, source.getCompetitors().get(competitorId).clone());
        }
        newEvent.setCompetitors(competitors);
    }

    public void updateEvent(Event toUpdate, Event source) throws InvocationTargetException, IllegalAccessException {
        boolean bettingEnabled = toUpdate.isBettingEnabled(); // we don't want to overwrite this variable!!
        this.copyProperties(toUpdate, source);
        toUpdate.setBettingEnabled(bettingEnabled);
        // NOTE: we won't bother to copy competitors because these shouldn't change
        copyMarkets(toUpdate, source);
    }

    private void copyMarkets(Event toUpdate, Event source) {
        if (toUpdate.getMarkets() == null && source.getMarkets() != null) {
            toUpdate.setMarkets(source.getMarkets());
        } else if (toUpdate.getMarkets() != null && source.getMarkets() != null) {
            source.getMarkets().values().forEach(sourceMarket -> {
                if (!toUpdate.getMarkets().containsKey(sourceMarket.getId())) {
                    if (sourceMarket.getDescription().equalsIgnoreCase("Moneyline") &&
                        sourceMarket.getDescriptionKey().equalsIgnoreCase("Head To Head") &&
                        inactiveMoneyLineMarketAlreadyExists(toUpdate.getMarkets())) {
                        markExistingMoneylineMarketInactive(toUpdate.getMarkets(), sourceMarket);
                    }
                    toUpdate.getMarkets().put(sourceMarket.getId(), sourceMarket);
                } else {
                    Market targetMarket = toUpdate.getMarkets().get(sourceMarket.getId());
                    try {
                        copyProperties(targetMarket, sourceMarket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    copyOutcomes(targetMarket, sourceMarket.getOutcomes());
                }
            });
        }

    }

    private void markExistingMoneylineMarketInactive(Map<String, Market> markets, Market sourceMarket) {
        Market existingMoneylineMarket = markets.values().stream()
                .filter(market -> market.getDescription().equalsIgnoreCase("Moneyline")
                        && market.getDescriptionKey().equalsIgnoreCase("Head to head"))
                .findFirst().get();
        existingMoneylineMarket.setDescription("(INACTIVE) Moneyline");
        existingMoneylineMarket.setDescriptionKey("(INACTIVE) Head to Head");
        existingMoneylineMarket.getOutcomes().values().forEach(outcome -> {
            outcome.setBettingEnabled(false);
        });
        if (existingMoneylineMarket.getBettingSession() != null) {
            boolean newBettingSessionInit = false;
            for (BettingSessionPosition existingBettingPosition : existingMoneylineMarket.getBettingSession().getPositions().values()) {
                if (existingBettingPosition.getBets() != null) {
                    for (Bet bet : existingBettingPosition.getBets()) {
                        String existingPositionOutcomeDescription = getOutcomeDescription(existingBettingPosition, existingMoneylineMarket);
                        String existingPositionOpposingOutcomeDescription = getOpposingOutcomeDescription(existingBettingPosition, existingMoneylineMarket);
                        String newOutcomeId = getCorrespondingOutcomeId(existingPositionOutcomeDescription, sourceMarket);
                        String newOpposingOutcomeId = getCorrespondingOutcomeId(existingPositionOpposingOutcomeDescription, sourceMarket);
                        if (!newBettingSessionInit) {
                            sourceMarket.initBettingSession(bet, newOutcomeId, newOpposingOutcomeId);
                            newBettingSessionInit = true;
                        } else {
                            sourceMarket.updateBettingSession(bet, newOutcomeId);
                        }
                    }
                }
            }
            existingMoneylineMarket.setBettingSession(null);
        }
    }

    private String getOpposingOutcomeDescription(BettingSessionPosition bettingSessionPosition, Market market) {
        return market.getOutcomes().get(bettingSessionPosition.getOpposingOutcomeId()).getDescription();
    }

    private String getCorrespondingOutcomeId(String oldOutcomeDescription, Market newMarket) {
        return newMarket.getOutcomes().values().stream().filter(newOutcome -> newOutcome.getDescription().equalsIgnoreCase(oldOutcomeDescription))
                .findFirst().get().getId();
    }

    private String getOutcomeDescription(BettingSessionPosition bettingSessionPosition, Market market) {
        return market.getOutcomes().get(bettingSessionPosition.getOutcomeId()).getDescription();
    }

    private boolean inactiveMoneyLineMarketAlreadyExists(Map<String, Market> markets) {
        return markets.values().stream().anyMatch(market -> {
           if (!market.getDescription().equalsIgnoreCase("Moneyline")
                   || !market.getDescriptionKey().equalsIgnoreCase("Head to head")) {
                return false;
           }
           if (market.getOutcomes().values().stream().noneMatch(outcome -> {
               return outcome.getPreviousPrices() != null;
           })) { // if there are no previous prices on the money line market, then it is probably inactive
               return true;
           }
           return market.getOutcomes().values().stream().noneMatch(outcome -> {
                // if there are previous prices on the money line market with a non-zero score,
                // we're not going to set it inactive.
                return outcome.getPreviousPrices().stream().anyMatch(price -> {
                    try {
                        return Integer.parseInt(price.getHomeScoreAtTimeOfPrice()) > 0 ||
                                Integer.parseInt(price.getVisitorScoreAtTimeOfPrice()) > 0 ||
                                price.getCurrentPeriodHomeScoreAtTimeOfPrice() > 0 ||
                                price.getCurrentPeriodVisitorScoreAtTimeOfPrice() > 0;
                    } catch (Exception e) {
                        return false;
                    }
                });
           });
        });
    }

    private void copyOutcomes(Market targetMarket, Map<String, Outcome> sourceOutcomes) {
        if (sourceOutcomes == null) {
            return;
        }
        else if (targetMarket.getOutcomes() == null) {
            targetMarket.setOutcomes(sourceOutcomes);
        }
        else {
            sourceOutcomes.values().forEach(sourceOutcome -> {
                if (!targetMarket.getOutcomes().containsKey(sourceOutcome.getId())) {
                    targetMarket.getOutcomes().put(sourceOutcome.getId(), sourceOutcome);
                } else {
                    Outcome targetOutcome = targetMarket.getOutcomes().get(sourceOutcome.getId());
                    try {
                        boolean bettingEnabled = targetOutcome.isBettingEnabled();
                        boolean forceBettingEnabled = targetOutcome.isForceBettingEnabled();
                        copyProperties(targetOutcome, sourceOutcome);
                        // need to preserve several properties from target
                        targetOutcome.setBettingEnabled(bettingEnabled);
                        targetOutcome.setForceBettingEnabled(forceBettingEnabled);
                        int currentPeriodVisitorScoreAtTimeOfPrice = targetOutcome.getPrice().getCurrentPeriodVisitorScoreAtTimeOfPrice();
                        int currentPeriodHomeScoreAtTimeOfPrice = targetOutcome.getPrice().getCurrentPeriodHomeScoreAtTimeOfPrice();
                        copyProperties(targetOutcome.getPrice(), sourceOutcome.getPrice());
                        targetOutcome.getPrice().setCurrentPeriodHomeScoreAtTimeOfPrice(currentPeriodHomeScoreAtTimeOfPrice);
                        targetOutcome.getPrice().setCurrentPeriodVisitorScoreAtTimeOfPrice(currentPeriodVisitorScoreAtTimeOfPrice);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


}
