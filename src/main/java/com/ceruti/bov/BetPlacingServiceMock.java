package com.ceruti.bov;

import com.ceruti.bov.model.Bet;
import com.ceruti.bov.model.Price;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.PrintStream;

@Component
@Profile("!live")
public class BetPlacingServiceMock implements BetPlacingService {

    @Autowired
    public BetPlacingServiceMock() {}

    public synchronized Bet placeBet(String outcomeId, Price price, double riskAmountInDollars) {
        int amountInCents = (int) (Math.ceil(riskAmountInDollars * 100));
        return placeBet(outcomeId, price, amountInCents);
    }

    public synchronized Bet placeBet(String outcomeId, Price price, int amountInCents) {
        Bet bet = new Bet(price, amountInCents / 100.0);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
        bet.markPlaced();
        return bet;
    }

    @Override
    public String getToken() {
        return "Authorization abcdefg";
    }

    @Override
    public void setToken(String token) {
        // do nothing
    }
}
