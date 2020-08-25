package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Bet;
import lahsivjar.spring.websocket.template.model.Outcome;
import lahsivjar.spring.websocket.template.model.Price;
import org.springframework.stereotype.Component;

@Component
public class BetPlacingService {

    // TODO: force delay between bets?

    public synchronized Bet placeBet(Outcome outcome, Price price, double riskAmount) {
        // TODO: send to bovada server
        // when successful...
        Bet bet = new Bet(price, riskAmount);
        bet.markPlaced();
        return bet;
    }

}
