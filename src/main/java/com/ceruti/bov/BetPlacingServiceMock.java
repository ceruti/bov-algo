package com.ceruti.bov;

import com.ceruti.bov.model.Bet;
import com.ceruti.bov.model.Price;
import com.ceruti.bov.util.ActiveProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.PrintStream;
import java.util.Date;

@Component
@Profile("!live")
public class BetPlacingServiceMock implements BetPlacingService {

    private ActiveProfileService activeProfileService;

    @Autowired
    public BetPlacingServiceMock(ActiveProfileService activeProfileService) {
        this.activeProfileService = activeProfileService;
    }

    @Override
    public Bet initBet(Price price, int amountInCents) {
        Bet bet = new Bet(price, amountInCents / 100.0);
        bet.setPlacedAt(new Date());
        return bet;
    }

    @Override
    public Bet submitBet(String outcomeId, Price price, int amountInCents, Bet bet) {
        if (!activeProfileService.isTestMode()) {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {

            }
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
