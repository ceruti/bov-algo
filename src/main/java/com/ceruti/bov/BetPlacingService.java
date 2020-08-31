package com.ceruti.bov;

import com.ceruti.bov.model.Bet;
import com.ceruti.bov.model.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BetPlacingService {

    private EventBook eventBook;

    @Autowired
    public BetPlacingService(EventBook eventBook) {
        this.eventBook = eventBook;
    }

    // TODO: force delay between bets?
    public synchronized Bet placeBet(String outcomeId, Price price, double riskAmountInDollars) {
        int amountInCents = (int) (Math.ceil(riskAmountInDollars * 100));
        return placeBet(outcomeId, price, amountInCents);
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
