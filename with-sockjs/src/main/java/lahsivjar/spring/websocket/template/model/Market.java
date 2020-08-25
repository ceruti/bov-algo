package lahsivjar.spring.websocket.template.model;

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
        if (initialBet.isPlaced()) {
            bettingSession = new BettingSession(initialBet, outcomeId, opposingOutcomeId);
        }
    }

    public void updateBettingSession(Bet additionalBet, String outcomeId) {
        bettingSession.update(additionalBet, outcomeId);
    }

}
