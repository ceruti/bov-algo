package com.ceruti.bov;

import com.ceruti.bov.model.Bet;
import com.ceruti.bov.model.Price;

public interface BetPlacingService {

    Bet initBet(Price price, int amountInCents);

    Bet submitBet(String outcomeId, Price price, int amountInCents, Bet bet);

    String getToken();

    void setToken(String token);
}
