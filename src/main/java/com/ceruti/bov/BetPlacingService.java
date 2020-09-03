package com.ceruti.bov;

import com.ceruti.bov.model.Bet;
import com.ceruti.bov.model.Price;

public interface BetPlacingService {

    Bet placeBet(String outcomeId, Price price, double riskAmountInDollars);

    Bet placeBet(String outcomeId, Price price, int amountInCents);

    String getToken();

    void setToken(String token);
}
