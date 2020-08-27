package lahsivjar.spring.websocket.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lahsivjar.spring.websocket.template.util.BettingFacilitatorService;
import lombok.Data;

import java.util.Map;

@Data
public class Market {

    private String description;
    private String descriptionKey;
    private String id;
    private String key;
    private String marketTypeId;
    private String notes;
    private Map<String, Outcome> outcomes;

    private BettingSession bettingSession;

    public void initBettingSession(Bet initialBet, String outcomeId, String opposingOutcomeId) {
        bettingSession = new BettingSession(initialBet, outcomeId, opposingOutcomeId);
    }

    public void updateBettingSession(Bet additionalBet, String outcomeId) {
        bettingSession.update(additionalBet, outcomeId);
    }

    private double getOutcomeProbability(String outcome1Id) {
        return getProbability(outcomes.get(outcome1Id));
    }

    public static double getProbability(Outcome outcome) {
        int americanOutcomeOdds = outcome.getPrice().getAmerican();
        if (americanOutcomeOdds > 0) {
            return (100.0) / (100.0 + americanOutcomeOdds);
        } else {
            return (1.0 * Math.abs(americanOutcomeOdds)) / (Math.abs(americanOutcomeOdds) + 100.0);
        }
    }

    // TODO: these are probably more suitably properties of the betting session... but we don't have references to the outcome objects

    @JsonProperty("expectedProfit")
    public double getExpectedProfit() {
        if (bettingSession != null) {
            return bettingSession.getNetProfitInOutcome1WinEvent() * getOutcomeProbability(bettingSession.getOutcome1Id())
                    + bettingSession.getNetProfitInOutcome2WinEvent() * getOutcomeProbability(bettingSession.getOutcome2Id());
        }
        return 0.0;
    }

    @JsonProperty("minimumProfit")
    public double getMinimumProfit() {
        if (bettingSession != null) {
            return bettingSession.getMinimumProfit();
        }
        return 0.0;
    }

    @JsonProperty("maximumProfit")
    public double getMaximumProfit() {
        if (bettingSession != null) {
            return bettingSession.getMaximumProfit();
        }
        return 0.0;
    }

    @JsonProperty("finalNetProfit")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Double getFinalNetProfit() {
        return bettingSession.getFinalNetProfit();
    }

    @JsonProperty("moreProfitableOutcomeDescription")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getMoreProfitableOutcomeDescription() {
        if (bettingSession != null) {
            String moreProfitableOutcomeId = bettingSession.getMoreProfitableOutcomeId();
            return outcomes.get(moreProfitableOutcomeId).getDescription();
        }
        return null;
    }

    @JsonProperty("lessProfitableOutcomeDescription")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getLessProfitableOutcomeDescription() {
        if (bettingSession != null) {
            String lessProfitableOutcomeId = bettingSession.getLessProfitableOutcomeId();
            return outcomes.get(lessProfitableOutcomeId).getDescription();
        }
        return null;
    }

}
