package lahsivjar.spring.websocket.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class BettingSession {

    private Map<String, BettingSessionPosition> positions = new HashMap<>();
    private String outcome1Id;
    private String outcome2Id;

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
        return Math.max(outcome1NetProfit(), outcome2NetProfit());
    }


    public double getMinimumProfit() {
        if (!validatePositions(positions)) {
            return 0;
        }
        return Math.min(outcome1NetProfit(), outcome2NetProfit());
    }

    private double outcome1NetProfit() {
        return positions.get(outcome1Id).getNetWinAmount() + positions.get(outcome2Id).getNetLoseAmount();
    }

    private double outcome2NetProfit() {
        return positions.get(outcome2Id).getNetWinAmount() + positions.get(outcome1Id).getNetLoseAmount();
    }

}
