package com.ceruti.bov;

import com.ceruti.bov.model.Bet;
import com.ceruti.bov.model.BetSlip;
import com.ceruti.bov.model.Price;
import org.springframework.http.ResponseEntity;

public interface BetPlacingService {

    Bet placeBet(String outcomeId, Price price, double riskAmountInDollars);

    Bet placeBet(String outcomeId, Price price, int amountInCents);

    String getToken();

    void setToken(String token);
}
