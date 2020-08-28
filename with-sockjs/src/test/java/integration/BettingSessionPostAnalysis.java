package integration;

import lahsivjar.spring.websocket.template.model.BettingSession;
import lahsivjar.spring.websocket.template.model.Market;
import lombok.Data;

@Data
public class BettingSessionAnalysis extends BettingSession {

    private BettingSession bettingSession;
    private Market market;
    private int favoriteChanges;

    public BettingSessionAnalysis(BettingSession bettingSession, Market market) {
        this.bettingSession = bettingSession;
        this.market = market;
    }

}
