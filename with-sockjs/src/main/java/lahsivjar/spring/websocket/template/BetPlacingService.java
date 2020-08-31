package lahsivjar.spring.websocket.template;

import lahsivjar.spring.websocket.template.model.Bet;
import lahsivjar.spring.websocket.template.model.Market;
import lahsivjar.spring.websocket.template.model.Outcome;
import lahsivjar.spring.websocket.template.model.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BetPlacingService {

    private EventBook eventBook;

    @Autowired
    public BetPlacingService(EventBook eventBook) {
        this.eventBook = eventBook;
    }

    // TODO: force delay between bets?
    public synchronized Bet placeBet(String outcomeId, Price price, double riskAmountInDollars) {
        return placeBet(outcomeId, price, Math.ceil(riskAmountInDollars * 100));
    }

    // TODO: force delay between bets?
    public synchronized Bet placeBet(String outcomeId, Price price, int amountInCents) {
        // TODO: send to bovada server
        // when successful...
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
        Bet bet = new Bet(price, amountInCents / 100.0);
        bet.markPlaced(); // or failed
        return bet;
    }

}
