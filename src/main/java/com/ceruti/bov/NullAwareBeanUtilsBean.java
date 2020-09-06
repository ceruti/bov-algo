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
                        copyProperties(targetOutcome, sourceOutcome);
                        targetOutcome.setBettingEnabled(bettingEnabled); // need to preserve this property from target
                        copyProperties(targetOutcome.getPrice(), sourceOutcome.getPrice());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


}
