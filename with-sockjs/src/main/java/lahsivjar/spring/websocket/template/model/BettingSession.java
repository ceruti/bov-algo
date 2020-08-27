package lahsivjar.spring.websocket.template.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class BettingSession {

    private Map<String, BettingSessionPosition> positions = new HashMap<>();
    private String outcome1Id;
    private String outcome2Id;
    private String winningOutcomeId;

    public BettingSession(Bet initialBet, String outcomeId, String opposingOutcomeId) {
        outcome1Id = outcomeId;
        outcome2Id = opposingOutcomeId;
        BettingSessionPosition bettingSessionPosition1 = new BettingSessionPosition(outcome1Id, outcome2Id, initialBet);
        BettingSessionPosition bettingSessionPosition2 = new BettingSessionPosition(outcome2Id, outcomeId);
        positions.put(outcome1Id, bettingSessionPosition1);
        positions.put(outcome2Id, bettingSessionPosition2);
    }

    public void update(Bet additionalBet, String outcomeId) {
        BettingSessionPosition bettingSessionPosition = positions.get(outcomeId);
        bettingSessionPosition.update(additionalBet);
    }

    private boolean validatePositions(Map<String, BettingSessionPosition> positions) {
        if (positions.size() != 2) {
            return false;
        }
        for (String outcomeId : positions.keySet()) {
            BettingSessionPosition position = positions.get(outcomeId);
            if (position.getOutcomeId().equalsIgnoreCase(position.getOpposingOutcomeId())) {
                return false;
            }
            if (!positions.containsKey(position.getOpposingOutcomeId())) {
                return false;
            }
        }
        if (!positions.containsKey(outcome1Id)) {
            return false;
        }
        if (!positions.containsKey(outcome2Id)) {
            return false;
        }
        return true;
    }

    public double getMaximumProfit() {
        if (!validatePositions(positions)) {
            return 0;
        }
        return Math.max(getNetProfitInOutcome1WinEvent(), getNetProfitInOutcome2WinEvent());
    }

    public Double getFinalNetProfit() {
        if (winningOutcomeId == null) {
            return null;
        }
        if (outcome1Id.equals(winningOutcomeId)) {
            return getNetProfitInOutcome1WinEvent();
        }
        if (outcome2Id.equals(winningOutcomeId)) {
            return getNetProfitInOutcome2WinEvent();
        }
        return null;
    }

    public double getMinimumProfit() {
        if (!validatePositions(positions)) {
            return 0;
        }
        return Math.min(getNetProfitInOutcome1WinEvent(), getNetProfitInOutcome2WinEvent());
    }

    Double getNetProfitInOutcome1WinEvent() {
        return positions.get(outcome1Id).getNetProfitInWinAmount() + positions.get(outcome2Id).getNetProfitInLossAmount();
    }

    Double getNetProfitInOutcome2WinEvent() {
        return positions.get(outcome2Id).getNetProfitInWinAmount() + positions.get(outcome1Id).getNetProfitInLossAmount();
    }

    public BettingSession clone() {
        BettingSession result = new BettingSession();
        result.outcome1Id = this.outcome1Id;
        result.outcome2Id = this.outcome2Id;
        result.winningOutcomeId = this.winningOutcomeId;
        for (String outcomeId : this.positions.keySet()) {
            BettingSessionPosition copy = this.positions.get(outcomeId).clone();
            result.positions.put(outcomeId, copy);
        }
        return result;
    }

    public String getMoreProfitableOutcomeId() {
        return getNetProfitInOutcome1WinEvent() >= getNetProfitInOutcome2WinEvent() ? outcome1Id : outcome2Id;
    }

    public String getLessProfitableOutcomeId() {
        return getNetProfitInOutcome1WinEvent() <= getNetProfitInOutcome2WinEvent() ? outcome1Id : outcome2Id;
    }

}
